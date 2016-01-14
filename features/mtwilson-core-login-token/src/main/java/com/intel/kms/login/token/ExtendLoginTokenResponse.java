/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.login.token;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="extend_login_token_response")
public class ExtendLoginTokenResponse extends LoginTokenResponse {
    public ExtendLoginTokenResponse() {
        super();
    }
}
