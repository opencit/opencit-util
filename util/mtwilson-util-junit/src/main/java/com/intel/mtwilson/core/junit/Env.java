/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.core.junit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jbuhacoff
 */
public class Env {
    
    /**
     * 
     * @param name without extension, so to read cit-attestation.properties just pass in "cit3-attestation"
     * @return
     * @throws IOException 
     */
    public static Properties getProperties(String name) throws IOException {
        File directory = new File(System.getProperty("user.home")+File.separator+".junit"+File.separator+"env");
        File file = directory.toPath().resolve(name+".properties").toFile();
        if( !file.exists() ) { return null; }
        try(FileInputStream in = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        }
    }

    /**
     * 
     * @param filename including extension, so to read cit-attestation.properties pass in "cit3-attestation.properties"
     * @return
     * @throws IOException 
     */
    public static String getString(String filename) throws IOException {
        File directory = new File(System.getProperty("user.home")+File.separator+".junit"+File.separator+"env");
        File file = directory.toPath().resolve(filename).toFile();
        if( !file.exists() ) { return null; }
        try(FileInputStream in = new FileInputStream(file)) {
            return IOUtils.toString(in, "UTF-8");
        }
    }
    
}
