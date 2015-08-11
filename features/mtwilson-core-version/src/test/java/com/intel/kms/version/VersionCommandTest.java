/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.version;

import com.intel.kms.version.cmd.Version;
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
}
