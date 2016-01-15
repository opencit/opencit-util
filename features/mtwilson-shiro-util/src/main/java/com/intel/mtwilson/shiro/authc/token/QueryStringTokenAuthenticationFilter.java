/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.token;

import com.intel.dcsg.cpg.http.Query;
import com.intel.mtwilson.shiro.authc.x509.*;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.util.WebUtils;
import com.intel.mtwilson.shiro.HttpAuthenticationFilter;
import com.intel.mtwilson.shiro.HttpQueryAuthenticationFilter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * Handles authentication via an query string parameter by looking up the user
 * and permissions associated with the token. Using tokens, it is possible to
 * grant temporary permissions to existing users without affecting their
 * permanent permission settings, or to grant temporary permissions to non-users
 * without requiring them to register as users.
 *
 * The token specified in the query parameter is a literal - it is not processed
 * in any way before passing to the lookup function.
 *
 * For example:
 * <pre>
 * &Authorization=token
 * </pre>
 *
 * Known issues: conformance with rfc7235
 *
 * @author jbuhacoff
 */
public class QueryStringTokenAuthenticationFilter extends HttpQueryAuthenticationFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(X509AuthenticationFilter.class);
    public static final String AUTHENTICATION_SCHEME = "Token";
    public static final String AUTHENTICATION_SCHEME_UC = "TOKEN";
    private boolean queryTokenEnabled = false;

    public QueryStringTokenAuthenticationFilter() {
        super();
        setAuthenticationScheme(AUTHENTICATION_SCHEME);
    }

    public void setQueryTokenEnabled(boolean queryTokenEnabled) {
        this.queryTokenEnabled = queryTokenEnabled;
    }

    public boolean isQueryTokenEnabled() {
        return queryTokenEnabled;
    }

    @Override
    protected boolean isAuthenticationRequest(ServletRequest request) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        return isQueryAuthenticationRequest(httpRequest);
    }

    /**
     *
     * @param httpRequest
     * @return
     */
    private Map<String, List<String>> parseQueryParameters(HttpServletRequest httpRequest) {
        String queryString = httpRequest.getQueryString();
        if (queryString == null) {
            return null;
        }
        try {
            Map<String, List<String>> queryParameters = Query.parse(queryString);
            return queryParameters;
        } catch (UnsupportedEncodingException e) {
            log.error("Cannot parse query string", e);
            return null;
        }
    }

    private boolean isQueryAuthenticationRequest(HttpServletRequest httpRequest) {
        String authorizationQueryParameterName = getQueryParameterName();
        if (authorizationQueryParameterName != null) {
            Map<String, List<String>> queryParameters = getQueryParameters();
            if (queryParameters == null) {
                queryParameters = parseQueryParameters(httpRequest);
            }
            if (queryParameters != null && queryParameters.containsKey(authorizationQueryParameterName)) {
                List<String> tokenValues = queryParameters.get(authorizationQueryParameterName);
                if (tokenValues != null && !tokenValues.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request) {
        log.debug("createToken");
        try {
            HttpServletRequest httpRequest = WebUtils.toHttp(request);
            Token token = getToken(httpRequest);

            TokenAuthenticationToken authenticationToken = new TokenAuthenticationToken(token, request.getRemoteAddr());
            log.debug("createToken: returning TokenAuthenticationToken");
            return authenticationToken;
        } catch (Exception e) {
            throw new AuthenticationException("Cannot authenticate request: " + e.getMessage(), e);
        }
    }

    private Token getToken(HttpServletRequest httpRequest) {
        Token tokenFromQuery = getTokenFromQuery(httpRequest);
        if (tokenFromQuery != null) {
            log.debug("Using token from query");
            return tokenFromQuery;
        }
        throw new IllegalArgumentException("No token in request");
    }

    private Token getTokenFromQuery(HttpServletRequest httpRequest) {
        String queryString = httpRequest.getQueryString();
        if (queryString != null) {
            try {
                String authorizationQueryParameterName = getQueryParameterName();
                Map<String, List<String>> queryParameters = getQueryParameters();
                if (queryParameters == null) {
                    queryParameters = parseQueryParameters(httpRequest);
                }
                if (queryParameters != null) {
                    List<String> authorizationTokens = queryParameters.get(authorizationQueryParameterName);
                    if (authorizationTokens != null && !authorizationTokens.isEmpty()) {
                        // we use only the first token provided
                        String tokenFromQuery = authorizationTokens.get(0);
                        log.debug("Got token from query: {}", tokenFromQuery);
                        return new Token(tokenFromQuery);
                    }
                }
            } catch (Exception e) {
                log.error("Cannot parse query string", e);
            }
        }
        return null;
    }
}
