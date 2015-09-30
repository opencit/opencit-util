/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

/**
 *
 * @author jbuhacoff
 */
public class ConfigurationException extends Exception {
    /**
     * 
     * @param message typically the name of the configuration setting
     */
    public ConfigurationException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message typically the name of the configuration setting
     * @param cause 
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
