/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.setup.faults;

import com.intel.dcsg.cpg.validation.Fault;

/**
 *
 * @author jbuhacoff
 */
public class IniEntryNotFound extends Fault {
    private String file;
    private String section;
    private String entry;

    public IniEntryNotFound(String file, String section, String entry) {
        super(String.format("%s[%s]%s", file, section, entry));
        this.file = file;
        this.section = section;
        this.entry = entry;
    }

    public String getFile() {
        return file;
    }

    public String getSection() {
        return section;
    }

    public String getEntry() {
        return entry;
    }
    
    
}
