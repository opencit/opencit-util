/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.login.token;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.shiro.UsernameWithPermissions;
import com.intel.mtwilson.shiro.authc.token.MemoryTokenRealm;
import com.intel.mtwilson.shiro.authc.token.TokenCredential;
import java.util.Collection;
import java.util.Date;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
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
     *        "not_before": 1447868402090,
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
    public CreateLoginTokenResponse createLoginToken(CreateLoginTokenRequest createLoginTokenRequest) {
        if( createLoginTokenRequest == null ) { log.error("Login token request data is missing"); throw new BadRequestException(); }
        
        Subject currentUser = SecurityUtils.getSubject();
        if (!currentUser.isAuthenticated()) {
            throw new WebApplicationException(Response.noContent().status(Response.Status.UNAUTHORIZED).build());
        }
        PrincipalCollection principals = currentUser.getPrincipals();
        Collection<UsernameWithPermissions> usernames = principals.byType(UsernameWithPermissions.class);
        UsernameWithPermissions usernameWithPermissions = LoginTokenUtils.getFirstElementFromCollection(usernames);
//        UserId userId = getFirstElementFromCollection(userIds);
//        LoginPasswordId loginPasswordId = getFirstElementFromCollection(loginPasswordIds);
        if ( usernameWithPermissions == null /* || userId == null || loginPasswordId == null */ ) {
            log.error("One of the required parameters is missing. Login request cannot be processed");
            throw new IllegalStateException();
        }

        MemoryTokenRealm.MemoryTokenDatabase database = MemoryTokenRealm.getDatabase();
        
        // a caller may request more than one token at a time;  we return tokens in the same order but also we include the original parameters to ensure the caller knows which token can do what...
        CreateLoginTokenResponse createLoginTokenResponse = new CreateLoginTokenResponse();
        for(LoginTokenAttributes attributes : createLoginTokenRequest.getData()) {
            if( attributes.getNotBefore() == null ) { attributes.setNotBefore(new Date()); }
            String tokenValue = RandomUtil.randomBase64String(32); // new random token value
            
            TokenCredential tokenCredential = new TokenCredential(tokenValue, attributes.getNotBefore(), attributes.getNotAfter(), 0, attributes.getNotMoreThan());
            
            database.add(tokenCredential, usernameWithPermissions);
            
            LoginTokenDescriptor loginTokenDescriptor = new LoginTokenDescriptor(tokenCredential.getValue(), attributes);
            createLoginTokenResponse.getData().add(loginTokenDescriptor);
        }
        return createLoginTokenResponse;
    }
}
