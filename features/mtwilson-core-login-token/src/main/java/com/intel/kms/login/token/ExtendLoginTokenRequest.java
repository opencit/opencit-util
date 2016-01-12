/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.login.token;

/**
 *
 * @author jbuhacoff
 */
public class ExtendLoginTokenRequest {
    private String authorizationToken;

    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public void setAuthorizationToken(String authorizationToken) {
        this.authorizationToken = authorizationToken;
    }
    
}
