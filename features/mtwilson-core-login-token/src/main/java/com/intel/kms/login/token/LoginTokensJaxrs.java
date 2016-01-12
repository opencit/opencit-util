/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.login.token;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.iso8601.Iso8601Date;
import com.intel.kms.login.token.faults.TokenNotFound;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.shiro.UsernameWithPermissions;
import com.intel.mtwilson.shiro.authc.token.MemoryTokenRealm;
import com.intel.mtwilson.shiro.authc.token.TokenCredential;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

/**
 *
 * @author jbuhacoff
 */
@V2
@Path("/login/tokens")
public class LoginTokensJaxrs {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoginTokensJaxrs.class);
    private Configuration configuration;

    public LoginTokensJaxrs() {
        log.debug("Initializing LoginTokensJaxrs");
        try {
            configuration = ConfigurationFactory.getConfiguration();
        } catch (IOException e) {
            log.warn("Cannot load configuration, using defaults", e);
            configuration = new PropertiesConfiguration();
        }
    }

    /**
     * The token is created using the caller's credentials, so callers can only
     * create tokens authorized with their own permissions.
     *
     * Note that an Authorization header must be provided with the request, and
     * can be anything supported by the server (Basic, Token, X509, etc)
     *
     * Example request:
     * <pre>
     * POST /login/tokens
     * Content-Type: application/json
     * Accept: application/json
     *
     * { "data": [ { "not_more_than": 1 } ] }
     * </pre>
     *
     * Example response:
     * <pre>
     * Content-Type: application/json
     * {
     *   "data": [{
     *     "token": "KrSX3iIbitCqInUqLY6Tjnq2xyfFTZUpykV11o3Wpgw=",
     *     "attributes": {
     *        "not_before": 1447869315451,
     *        "not_after": 1447871115451,
     *        "not_more_than": 1
     *     }
     *   }]
     * }
     * </pre>
     *
     * @param createLoginTokenRequest
     * @return
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @RequiresPermissions("login_token:create")
    public CreateLoginTokenResponse createLoginToken(CreateLoginTokenRequest createLoginTokenRequest, @Context final HttpServletRequest httpRequest) {
        log.debug("createLoginToken");
        if (createLoginTokenRequest == null) {
            log.error("Login token request data is missing");
            throw new BadRequestException();
        }

        boolean requireTls = Boolean.valueOf(configuration.get(LoginTokenUtils.LOGIN_REQUIRES_TLS, "true"));
        if (requireTls && !httpRequest.isSecure()) {
            log.info("Denying non-TLS login request");
            throw new WebApplicationException(Response.noContent().status(Response.Status.UNAUTHORIZED).build());
        }

        Subject currentUser = SecurityUtils.getSubject();
        if (!currentUser.isAuthenticated()) {
            throw new WebApplicationException(Response.noContent().status(Response.Status.UNAUTHORIZED).build());
        }
        PrincipalCollection principals = currentUser.getPrincipals();
        Collection<UsernameWithPermissions> usernames = principals.byType(UsernameWithPermissions.class);
        UsernameWithPermissions usernameWithPermissions = LoginTokenUtils.getFirstElementFromCollection(usernames);
//        UserId userId = getFirstElementFromCollection(userIds);
//        LoginPasswordId loginPasswordId = getFirstElementFromCollection(loginPasswordIds);
        if (usernameWithPermissions == null /* || userId == null || loginPasswordId == null */) {
            log.error("One of the required parameters is missing. Login request cannot be processed");
            throw new IllegalStateException();
        }

        MemoryTokenRealm.MemoryTokenDatabase database = MemoryTokenRealm.getDatabase();

        // a caller may request more than one token at a time;  we return tokens in the same order but also we include the original parameters to ensure the caller knows which token can do what...
        CreateLoginTokenResponse createLoginTokenResponse = new CreateLoginTokenResponse();
        for (LoginTokenAttributes attributes : createLoginTokenRequest.getData()) {
            Date notBefore = attributes.getNotBefore();
            Date notAfter = attributes.getNotAfter();
            Integer notMoreThan = attributes.getNotMoreThan();

            if (notBefore == null) {
                notBefore = new Date();
            }
            if (notAfter == null) {
                notAfter = LoginTokenUtils.getExpirationDate(notBefore, configuration);
            }

            String tokenValue = RandomUtil.randomBase64String(32); // new random token value
            TokenCredential tokenCredential = new TokenCredential(tokenValue, notBefore, notAfter, 0, notMoreThan);
            database.add(tokenCredential, usernameWithPermissions);

            attributes.setNotBefore(notBefore);
            attributes.setNotAfter(notAfter);
            attributes.setNotMoreThan(notMoreThan);
            LoginTokenDescriptor loginTokenDescriptor = new LoginTokenDescriptor(tokenCredential.getValue(), attributes);
            createLoginTokenResponse.getData().add(loginTokenDescriptor);
        }
        return createLoginTokenResponse;
    }

    /**
     * Unlike standard v2 resources, we intentionally do not put the token value
     * in the URL.  So it's 
     * <pre>
     * POST /login/tokens/extend
     * Content-Type: application/json
     * 
     * { "authorization_token": "..." }
     * </pre>
     * 
     * Example successful response:
     * <pre>
     * {
     *   "authorization_token":"x3nNGQ4pcEH9aKINGZ+mTjB+oUmTdw3cKvK2rfM7O+U=",
     *   "not_after":"2016-01-12T02:58:01-0800",
     *   "faults":[]
     * }
     * </pre>
     * 
     * Example failure response:
     * <pre>
     * {
     *   "faults":[
     *     {
     *       "type":"com.intel.kms.login.token.faults.TokenNotFound",
     *       "description":"z3nNGQ4pcEH9aKINGZ+mTjB+oUmTdw3cKvK2rfM7O+U="
     *     }
     *   ]
     * }
     * </pre>
     * @param tokenExtendRequest
     * @param request
     * @param response
     * @return 
     */
    @POST
    @Path("/extend")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ExtendLoginTokenResponse extendTokenExpiration(ExtendLoginTokenRequest tokenExtendRequest, @Context final HttpServletRequest request, @Context final HttpServletResponse response) {
        log.debug("extendTokenExpiration");
        ExtendLoginTokenResponse extendTokenResponse = new ExtendLoginTokenResponse();
        MemoryTokenRealm.MemoryTokenDatabase database = MemoryTokenRealm.getDatabase();
        TokenCredential tokenCredential = database.findCredentialByTokenValue(tokenExtendRequest.getAuthorizationToken());
        if( tokenCredential == null ) {
            extendTokenResponse.getFaults().add(new TokenNotFound(tokenExtendRequest.getAuthorizationToken()));
            return extendTokenResponse;
        }
        log.info("Expiry date before enxtension  :  " + tokenCredential.getNotAfter());
        TokenCredential updatedToken = new TokenCredential(tokenCredential.getValue(), tokenCredential.getNotBefore(), LoginTokenUtils.getExpirationDate(tokenCredential.getNotAfter(), configuration), tokenCredential.getUsed(), tokenCredential.getUsedMax());
        database.update(tokenCredential.getValue(), updatedToken);
//        tokenCredential.setNotAfter(LoginTokenUtils.getExpirationDate(new Date(), configuration));
        log.info("Expiry date extended to :  " + updatedToken.getNotAfter());

        // include token in response headers      
        response.addHeader("Authorization-Token", tokenCredential.getValue());
        response.addHeader("Date", Iso8601Date.format(new Date()));
        extendTokenResponse.setAuthorizationToken(tokenCredential.getValue());
        extendTokenResponse.setNotAfter(new Iso8601Date(tokenCredential.getNotAfter()));
        return extendTokenResponse;
    }
}
