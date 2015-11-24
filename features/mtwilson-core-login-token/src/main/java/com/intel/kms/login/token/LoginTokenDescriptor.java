/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.login.token;

/**
 * A container class to store both a token and its attributes
 * @author jbuhacoff
 */
public class LoginTokenDescriptor {
    private String token;
    private LoginTokenAttributes attributes;

    public LoginTokenDescriptor() {
    }

    public LoginTokenDescriptor(String token, LoginTokenAttributes attributes) {
        this.token = token;
        this.attributes = attributes;
    }

    public LoginTokenAttributes getAttributes() {
        return attributes;
    }

    public String getToken() {
        return token;
    }

    public void setAttributes(LoginTokenAttributes attributes) {
        this.attributes = attributes;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
    
}
