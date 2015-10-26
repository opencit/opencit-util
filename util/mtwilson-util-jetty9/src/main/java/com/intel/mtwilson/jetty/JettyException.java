/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jetty;

/**
 *
 * @author jbuhacoff
 */
public class JettyException extends Exception {
    /**
     * 
     * @param message providing some context
     */
    public JettyException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message providing some context
     * @param cause 
     */
    public JettyException(String message, Throwable cause) {
        super(message, cause);
    }
}
