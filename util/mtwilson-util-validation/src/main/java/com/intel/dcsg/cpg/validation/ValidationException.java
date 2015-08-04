/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.validation;

import java.util.Collection;

/**
 *
 * @author jbuhacoff
 */
public class ValidationException extends RuntimeException implements Faults {
    private Collection<Fault> faults;
    
    public ValidationException() {
        super();
    }
    /*
    public ValidationException(String message) {
        super(message);
    }
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    public ValidationException(Throwable cause) {
        super(cause);
    }
    */
    public ValidationException(Collection<Fault> faults) {
        super();
        this.faults = faults;
    }

    @Override
    public Collection<Fault> getFaults() {
        return faults;
    }
}
