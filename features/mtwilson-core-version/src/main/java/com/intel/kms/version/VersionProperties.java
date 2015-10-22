/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.version;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author jbuhacoff
 */
public class VersionProperties {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VersionProperties.class);
    public static final String VERSION_PROPERTIES_PATH = "/com/intel/mtwilson/version.properties";
    
     public static Properties getVersionProperties() throws IOException {
        try(InputStream in = VersionProperties.class.getResourceAsStream(VERSION_PROPERTIES_PATH)) {
            Properties versionProperties = new Properties();
            if( in == null ) { log.debug("Version properties not found in classpath: {}", VERSION_PROPERTIES_PATH); return versionProperties; }
            versionProperties.load(in);
            return versionProperties;
        }
     }
}
