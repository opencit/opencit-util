/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.login.token.faults;

import com.intel.dcsg.cpg.validation.Fault;

/**
 *
 * @author jbuhacoff
 */
public class TokenNotFound extends Fault {
    public TokenNotFound(String token) {
        super(token);
    }
}
