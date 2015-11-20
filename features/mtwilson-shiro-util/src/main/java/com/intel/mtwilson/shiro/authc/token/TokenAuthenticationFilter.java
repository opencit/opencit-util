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
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * Handles authentication via an HTTP Authorization header with the "Token" 
 * keyword by looking up the user and permissions associated with the token.
 * Using tokens, it is possible to grant temporary permissions to
 * existing users without affecting their permanent permission settings,
 * or to grant temporary permissions to non-users without requiring them
 * to register as users.
 * 
 * The token specified in the authorization header is a literal - it is not
 * processed in any way before passing to the lookup function.
 * 
 * For example:
 * <pre>
 * Authorization: Token {token}
 * </pre>
 * 
 * Known issues: conformance with rfc7235
 * 
 * @author jbuhacoff
 */
public class TokenAuthenticationFilter extends HttpAuthenticationFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(X509AuthenticationFilter.class);
    public static final String AUTHENTICATION_SCHEME = "Token";
    public static final String AUTHENTICATION_SCHEME_UC = "TOKEN";

    public TokenAuthenticationFilter() {
        super();
        setAuthenticationScheme(AUTHENTICATION_SCHEME);
    }

    @Override
    protected boolean isAuthenticationRequest(ServletRequest request) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        return isHeaderAuthenticationRequest(httpRequest);
    }
    
    private boolean isHeaderAuthenticationRequest(HttpServletRequest httpRequest) {
        String authorizationHeaderName = getAuthorizationHeaderName();
        if( authorizationHeaderName != null ) {
            String authorizationHeaderValue = httpRequest.getHeader(authorizationHeaderName);
            if( authorizationHeaderValue != null ) {
                return authorizationHeaderValue.toUpperCase().startsWith(AUTHENTICATION_SCHEME_UC+" ");
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
        Token tokenFromHeader = getTokenFromHeader(httpRequest);
        if( tokenFromHeader != null ) {
            log.debug("Using token from header");
            return tokenFromHeader;
        }
        throw new IllegalArgumentException("No token in request");
    }
    
    private Token getTokenFromHeader(HttpServletRequest httpRequest) {
        String authorizationHeader = httpRequest.getHeader(getAuthorizationHeaderName());
        log.debug("Parsing authorization header: {}", authorizationHeader);
        // splitting on spaces should yield "Token" followed by a literal
        if( authorizationHeader != null ) {
            String[] terms = authorizationHeader.split(" ");
            if( terms.length == 2 && AUTHENTICATION_SCHEME_UC.equals(terms[0].toUpperCase()) && !terms[1].isEmpty() ) {
                String tokenFromHeader = terms[1];
                log.debug("Got token from header: {}", tokenFromHeader);
                return new Token(tokenFromHeader);
            }
            else {
                log.debug("Authorization header is not token");
            }
        }        
        return null;
    }

}
