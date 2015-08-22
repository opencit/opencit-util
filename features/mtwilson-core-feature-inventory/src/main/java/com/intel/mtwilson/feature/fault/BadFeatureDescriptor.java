/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.feature.fault;

import com.intel.dcsg.cpg.validation.Fault;

/**
 * When the "feature.xml" file cannot be read
 * @author jbuhacoff
 */
public class BadFeatureDescriptor extends Fault {
    private String featureId;
    public BadFeatureDescriptor(String featureId) {
        super(featureId);
    }
    public BadFeatureDescriptor(Throwable cause, String featureId) {
        super(cause, featureId);
    }

    public String getFeatureId() {
        return featureId;
    }
    
}
