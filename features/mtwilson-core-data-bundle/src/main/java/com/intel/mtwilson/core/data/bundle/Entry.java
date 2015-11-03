/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.core.data.bundle;

/**
 * Represents a single file in the bundle.
 * 
 * @author jbuhacoff
 */
public class Entry {
    private String path;
    private byte[] content;

    public Entry(String path, byte[] content) {
        this.path = path;
        this.content = content;
    }

    public byte[] getContent() {
        return content;
    }

    public String getPath() {
        return path;
    }
    
}
