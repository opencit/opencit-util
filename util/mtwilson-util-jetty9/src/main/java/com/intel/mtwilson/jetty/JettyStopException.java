/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jetty;

/**
 *
 * @author jbuhacoff
 */
public class JettyStopException extends JettyException {
    /**
     * 
     * @param message providing some context
     */
    public JettyStopException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message providing some context
     * @param cause 
     */
    public JettyStopException(String message, Throwable cause) {
        super(message, cause);
    }
}
