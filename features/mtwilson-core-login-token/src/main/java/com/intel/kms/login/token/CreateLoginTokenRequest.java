/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.login.token;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;

/**
 *
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class CreateLoginTokenRequest {
    private ArrayList<LoginTokenAttributes> data = new ArrayList<>();

    public CreateLoginTokenRequest() {
    }

    
    public ArrayList<LoginTokenAttributes> getData() {
        return data;
    }

}
