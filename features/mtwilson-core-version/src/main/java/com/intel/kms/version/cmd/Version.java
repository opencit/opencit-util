/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.version.cmd;

import com.intel.dcsg.cpg.console.AbstractCommand;
import com.intel.kms.version.VersionInfo;
import com.intel.kms.version.VersionProperties;
import java.io.IOException;
import java.util.Properties;


/**
 *
 * @author jbuhacoff
 */
public class Version extends AbstractCommand {
    private static final VersionInfo UNKNOWN = new VersionInfo("unknown", "unknown", "unknown");

    @Override
    public void execute(String[] args) throws Exception {
        VersionInfo versionInfo = getVersionInfo();
        System.out.println(String.format("Version: %s\nBranch: %s\nTimestamp: %s\n", versionInfo.getVersion(), versionInfo.getBranch(), versionInfo.getTimestamp()));
    }

    public VersionInfo getVersionInfo() {
        try {
            Properties versionProperties = VersionProperties.getVersionProperties();
            return VersionInfo.fromProperties(versionProperties);
        }
        catch(IOException e) {
            log.error("Cannot read version.properties", e);
            return UNKNOWN;
        }
        
    }
}
