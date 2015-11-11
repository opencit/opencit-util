/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.password;

import com.intel.mtwilson.shiro.authc.x509.*;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.util.WebUtils;
import com.intel.mtwilson.shiro.HttpAuthenticationFilter;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.codec.Base64;

/**
 * REPLACES HttpBasicAuthenticationFilter with different rules about how to
 * process the request, that allow it to work better in combination with the
 * TokenAuthenticationFilter and also allowing unauthenticated users to
 * access the API in case of public methods that don't require permissions.
 * 
 * Handles authentication via an HTTP Authorization header with the "Basic" 
 * keyword by looking up the user and permissions associated with the given
 * username and password, and checking the password against the stored hash.
 * 
 * For example:
 * <pre>
 * Authorization: Basic {base-64 of username:password}
 * </pre>
 * 
 * @author jbuhacoff
 */
public class PasswordAuthenticationFilter extends HttpAuthenticationFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(X509AuthenticationFilter.class);

    public PasswordAuthenticationFilter() {
        super();
        setAuthenticationScheme("Basic");
    }

    @Override
    protected UsernamePasswordToken createToken(ServletRequest request) {
        log.debug("createToken");
        try {
            HttpServletRequest httpRequest = WebUtils.toHttp(request);
            UsernamePasswordToken authenticationToken = getToken(httpRequest);
            log.debug("createToken: returning UsernamePasswordToken");
            return authenticationToken;
        } catch (Exception e) {
            throw new AuthenticationException("Cannot authenticate request: " + e.getMessage(), e);
        }
    }

    private UsernamePasswordToken getToken(HttpServletRequest httpRequest) {
        String authorizationText = httpRequest.getHeader(getAuthorizationHeaderName());
        log.debug("Parsing authorization header: {}", authorizationText);
        
        // splitting on spaces should yield "Basic" followed by a literal
        String[] terms = authorizationText.split(" ");
        if( terms.length == 0 ) {
            throw new IllegalArgumentException("Authorization header is empty");
        }
        if (!"BASIC".equals(terms[0].toUpperCase())) {
            throw new IllegalArgumentException("Authorization type is not Basic");
        }
        if( terms.length != 2 ) {
            throw new IllegalArgumentException("Authorization header format invalid for Basic");
        }
        if( terms[1].isEmpty() ) {
            throw new IllegalArgumentException("Username and password are missing");
        }
        log.debug("Got token {}", terms[1]);
        String decoded = Base64.decodeToString(terms[1]);
        String username = "";
        String password = "";
        int separatorIndex = decoded.indexOf(":");
        if( separatorIndex != -1 ) {
            username = decoded.substring(0, separatorIndex);
            if( separatorIndex < decoded.length() ) {
                password = decoded.substring(separatorIndex+1);
            }
        }
        return new UsernamePasswordToken(username, password, false, httpRequest.getRemoteAddr());
    }

}
