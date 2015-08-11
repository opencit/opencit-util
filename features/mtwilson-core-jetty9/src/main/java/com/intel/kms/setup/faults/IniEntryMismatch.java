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
public class IniEntryMismatch extends Fault {
    private String file;
    private String section;
    private String entry;
    private String comparison;
    
    public IniEntryMismatch(String file, String section, String entry, String comparison) {
        super(String.format("%s[%s]%s mismatch with %s", file, section, entry, comparison));
        this.file = file;
        this.section = section;
        this.entry = entry;
        this.comparison = comparison;
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

    public String getComparison() {
        return comparison;
    }
    
    
}
