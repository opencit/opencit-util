/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.login.token;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.validation.Faults;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class CreateLoginTokenResponse implements Faults {
    private ArrayList<Fault> faults = new ArrayList<>();
    private ArrayList<LoginTokenDescriptor> data = new ArrayList<>();
    
    @Override
    public Collection<Fault> getFaults() {
        return faults;
    }

    public ArrayList<LoginTokenDescriptor> getData() {
        return data;
    }
    
    
}
