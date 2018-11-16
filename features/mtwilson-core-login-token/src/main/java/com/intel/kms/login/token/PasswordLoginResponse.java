/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.login.token;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="password_login_response")
public class PasswordLoginResponse extends LoginTokenResponse {
    public PasswordLoginResponse() {
        super();
    }
}
