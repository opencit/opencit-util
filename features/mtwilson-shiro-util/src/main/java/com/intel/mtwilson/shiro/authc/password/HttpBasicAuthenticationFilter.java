/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.password;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 *
 * @author jbuhacoff
 */
public class HttpBasicAuthenticationFilter extends BasicHttpAuthenticationFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HttpBasicAuthenticationFilter.class);
    private boolean sendChallenge = true;

    public HttpBasicAuthenticationFilter() {
        super();
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        AuthenticationToken token = createToken(request, response);
        if (token == null) {
            String msg = "createToken method implementation returned null. A valid non-null AuthenticationToken "
                    + "must be created in order to execute a login attempt.";
            throw new IllegalStateException(msg);
        }
        try {
            Subject subject = getSubject(request, response);
            log.debug("executeLogin subject {}", subject.getClass().getName());
            subject.login(token);
            return onLoginSuccess(token, subject, request, response);
        } catch (AuthenticationException e) {
            log.debug("executeLogin subject login failed {}", e);
            return onLoginFailure(token, e, request, response);
        }
    }
    
    /**
     * Disabling this setting will skip sending the WWW-Authenticate header. 
     * By default this setting is enabled, using the superclass sendChallenge
     * implementation.
     * The example configuration below will send 401 Unauthorized but will
     * not send WWW-Authenticate:
     * <pre>
authcPassword=com.intel.mtwilson.shiro.authc.password.HttpBasicAuthenticationFilter
authcPassword.authzScheme=Basic
authcPassword.sendChallenge=false
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
    
    @Override
    protected boolean sendChallenge(ServletRequest request, ServletResponse response) {
        if( sendChallenge ) {
            return super.sendChallenge(request, response);
        }
        else {
            HttpServletResponse httpResponse = WebUtils.toHttp(response);
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
}
