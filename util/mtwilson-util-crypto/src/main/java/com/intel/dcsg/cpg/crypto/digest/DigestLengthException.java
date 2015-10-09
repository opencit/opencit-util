/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.digest;

/**
 * Thrown when a digest value is passed in which is not the correct length for
 * its algorithm; for example 40 hex characters passed to MD5 would throw this
 * exception because MD5 expects 32 hex characters.
 * @author jbuhacoff
 */
public class DigestLengthException extends IllegalArgumentException {
    private String algorithm;
    private int expected;
    private int actual;
    
    public DigestLengthException(String algorithm, int expected, int actual) {
        super(algorithm);
        this.algorithm = algorithm;
        this.expected = expected;
        this.actual = actual;
    }

    public DigestLengthException(String algorithm, int expected, int actual, Throwable cause) {
        super(algorithm, cause);
        this.algorithm = algorithm;
        this.expected = expected;
        this.actual = actual;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public int getExpected() {
        return expected;
    }

    public int getActual() {
        return actual;
    }
    
    
    
}
