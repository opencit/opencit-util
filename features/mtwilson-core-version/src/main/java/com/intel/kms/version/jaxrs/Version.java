/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.version.jaxrs;

import com.intel.kms.version.VersionInfo;
import com.intel.kms.version.VersionProperties;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Properties;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;

/**
 * Reads a file from the classpath /com/intel/mtwilson/version.properties
 * This file should be created by the main application package (kms-zip) 
 * which controls the overall version for the application.
 * Possibly reading the version.properties file could move to the launcher
 * and this class could then just read it from system properties set by
 * the launcher.
 * 
 * Example content of version.properties:
 * <pre>
mtwilson.version=${project.version}
mtwilson.timestamp=${build.timestamp}
mtwilson.git.branch=${git.branch}
mtwilson.git.commit=${git.commit.id}
 * </pre>
 * 
 * @author jbuhacoff
 */
@V2
@Path("/version")
public class Version {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Version.class);
    private static final VersionInfo UNKNOWN = new VersionInfo(); // all fields null
    
    @GET
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public VersionInfo getVersion() {
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
