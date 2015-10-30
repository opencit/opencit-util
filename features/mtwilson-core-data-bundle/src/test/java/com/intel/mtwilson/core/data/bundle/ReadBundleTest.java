/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.core.data.bundle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class ReadBundleTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReadBundleTest.class);

    @Test
    public void readMtWilsonConfigurationBundle() throws FileNotFoundException, IOException {
        InputStream in = getClass().getResourceAsStream("/mtwilson-configuration-databundle.tgz");
        if( in == null ) { throw new FileNotFoundException("mtwilson-configuration-databundle.tgz"); }
        TarGzipBundle bundle = new TarGzipBundle();
        bundle.read(in);
        
    }
}
