/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.version;

// {"timestamp": "2014-07-16T15:37:28.702-0700", "version": "1.0-SNAPSHOT", "branch": "release-1.0"}

import java.util.Properties;


public class VersionInfo {
    private String version;
    private String branch;
    private String timestamp;

    public VersionInfo() {
    }

    
    public VersionInfo(String version, String branch, String timestamp) {
        this.version = version;
        this.branch = branch;
        this.timestamp = timestamp;
    }
    
    

    public String getVersion() {
        return version;
    }

    public String getBranch() {
        return branch;
    }

    public String getTimestamp() {
        return timestamp;
    }
    
    public static VersionInfo fromProperties(Properties versionProperties) {
        VersionInfo versionInfo = new VersionInfo();
        versionInfo.version = versionProperties.getProperty("mtwilson.version");
        versionInfo.branch = versionProperties.getProperty("mtwilson.git.branch", versionProperties.getProperty("mtwilson.branch"));
        versionInfo.timestamp = versionProperties.getProperty("mtwilson.timestamp");
        return versionInfo;
    }
    
}
