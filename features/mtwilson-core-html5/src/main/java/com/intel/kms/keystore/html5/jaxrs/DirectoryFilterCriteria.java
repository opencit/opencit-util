/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.keystore.html5.jaxrs;

import javax.ws.rs.QueryParam;

/**
 *
 * @author jbuhacoff
 */
public class DirectoryFilterCriteria {
    @QueryParam("feature")
    public String featureId;
}
