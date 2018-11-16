/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

import com.intel.dcsg.cpg.http.Query;
import com.intel.dcsg.cpg.io.ErrorUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.web.util.WebUtils;

/**
 *
 * @author jbuhacoff
 */
public abstract class HttpQueryAuthenticationFilter extends AuthenticationFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HttpQueryAuthenticationFilter.class);
    private String queryParameterName = "Authorization";
    private String challengeHeaderName = "WWW-Authenticate";
    private String authenticationScheme = null; // for example X509, BASIC, DIGEST
    private String applicationName = "Mt Wilson";
    private boolean sendChallenge = true;
    private Map<String, List<String>> queryParameters;

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setAuthenticationScheme(String authenticationScheme) {
        this.authenticationScheme = authenticationScheme;
    }

    public void setQueryParameterName(String requestQueryParameterName) {
        this.queryParameterName = requestQueryParameterName;
    }

    public void setChallengeHeaderName(String responseAuthenticateHeaderName) {
        this.challengeHeaderName = responseAuthenticateHeaderName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getAuthenticationScheme() {
        return authenticationScheme;
    }

    public String getQueryParameterName() {
        return queryParameterName;
    }

    public String getChallengeHeaderName() {
        return challengeHeaderName;
    }

    public Map<String, List<String>> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(Map<String, List<String>> queryParameters) {
        this.queryParameters = queryParameters;
    }
    
    /**
     * Disabling this setting will skip sending the WWW-Authenticate header. By
     * default this setting is enabled. The example configuration below will
     * send 401 Unauthorized but will not send WWW-Authenticate:
     * <pre>
     * authcPassword=com.intel.mtwilson.shiro.authc.password.PasswordAuthenticationFilter
     * authcPassword.sendChallenge=false
     * </pre>
     *
     * @param sendChallenge
     */
    public void setSendChallenge(boolean sendChallenge) {
        this.sendChallenge = sendChallenge;
    }

    public boolean isSendChallenge() {
        return sendChallenge;
    }

    /**
     * Looks for an Authorization header (the name can be set with
     * setAuthorizationHeaderName) with the authenticationScheme
     *
     * @param request
     * @return
     */
    @Override
    protected boolean isAuthenticationRequest(ServletRequest request) {
        if (queryParameterName == null) {
            throw new IllegalStateException("A filter extending HttpQueryAuthenticationFilter must set the query parameter name; default is Authorization");
        }
        if (authenticationScheme == null) {
            throw new IllegalStateException("A filter extending HttpAuthenticationFilter must set the authentication scheme");
        }
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        log.debug("isAuthenticationRequest looking for {} in {}", queryParameterName, httpRequest.getQueryString());
        try {
            queryParameters = Query.parse(httpRequest.getQueryString());
            if (queryParameters.containsKey(queryParameterName)) {
                return true;
            }
        } catch (UnsupportedEncodingException e) {
            log.error("Cannot parse query string", e);
        }
        log.debug("isAuthenticationRequest did not find query parameter {} header", queryParameterName);
        return false;
    }

    protected void sendChallenge(ServletRequest request, ServletResponse response) {
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        if (sendChallenge) {
            log.debug("sending challenge header {} to {}", challengeHeaderName, request.getRemoteAddr());
            String authcHeader = authenticationScheme + " realm=\"" + getApplicationName() + "\"";
            httpResponse.addHeader(challengeHeaderName, authcHeader);
        }
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException authenticationException, ServletRequest request, ServletResponse response) {
        log.debug("onLoginFailure token: {} exception: {}: {}", token, authenticationException.getClass().getName(), authenticationException.getMessage());
        if (authenticationException.getCause() != null) {
            log.debug("authentication exception cause", authenticationException.getCause());
        }
        try {
            sendChallenge(request, response);
        } catch (Exception e) {
            log.error("Error while sending challenge due to login failure: {}", e.getMessage());
            log.debug("Error while sending challenge", e);
        }
        return false;
    }

    /**
     * The UnauthenticatedException is thrown from authorization code within the
     * business layer that requires the user to be authenticated and the user is
     * not - that can only happen if the client did not attempt to send
     * authentication tokens with the request.
     *
     * @param request
     * @param response
     * @param existing
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void cleanup(ServletRequest request, ServletResponse response, Exception existing) throws ServletException, IOException {
        UnauthenticatedException cause = ErrorUtil.findCause(existing, UnauthenticatedException.class);
        if (cause != null) {
            log.debug("Caught UnauthenticatedException; sending challenge");
            try {
                sendChallenge(request, response);
                existing = null;
            } catch (Exception e) {
                existing = e;
            }
        }
        super.cleanup(request, response, existing);
    }
}
