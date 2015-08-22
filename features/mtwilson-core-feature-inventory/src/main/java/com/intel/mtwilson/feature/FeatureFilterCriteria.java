/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.feature;

import javax.ws.rs.QueryParam;

/**
 *
 * @author jbuhacoff
 */
public class FeatureFilterCriteria {
    @QueryParam("id")
    public String featureId;
    
    @QueryParam("extends")
    public String featureExtends;
}
