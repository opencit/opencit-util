/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.version;

import com.intel.kms.version.cmd.Version;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class VersionCommandTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VersionCommandTest.class);

    @Test
    public void testVersionCommand() throws Exception {
       Version cmd = new Version(); 
       cmd.execute(new String[0]);
    }
    
    @Test
    public void testNullInputStreamAutoClose() throws IOException {
        try(InputStream in = null) {
            log.debug("testNullInputStreamAutoClose inside try");
        }
        log.debug("testNullInputStreamAutoClose after try");
    }
}
