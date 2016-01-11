/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io.file;

import java.io.File;
import java.io.FileFilter;

/**
 * An implementation of {@code java.io.FileFilter} that only accepts files
 * (not directories).
 */
public class FileOnlyFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
        return pathname.isFile();
    }
    
}
