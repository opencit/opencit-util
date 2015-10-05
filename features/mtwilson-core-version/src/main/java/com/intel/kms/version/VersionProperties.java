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
        InputStream in = VersionProperties.class.getResourceAsStream(VERSION_PROPERTIES_PATH);        
        if (in == null) {
            log.debug("Version properties not found in classpath: {}", VERSION_PROPERTIES_PATH);
            return  new Properties();
        }
        try {
            Properties versionProperties = new Properties();
            versionProperties.load(in);
            return versionProperties;
        }
        finally {
            in.close();
        }
    }
}
