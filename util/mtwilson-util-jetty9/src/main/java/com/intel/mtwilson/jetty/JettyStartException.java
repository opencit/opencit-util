/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jetty;

/**
 *
 * @author jbuhacoff
 */
public class JettyStartException extends JettyException {
    /**
     * 
     * @param message providing some context
     */
    public JettyStartException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message providing some context
     * @param cause 
     */
    public JettyStartException(String message, Throwable cause) {
        super(message, cause);
    }
}
