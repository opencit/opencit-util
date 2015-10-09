/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcg.io;

import com.intel.dcsg.cpg.io.Copyable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class ClosableTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClosableTest.class);

    @Test
    public void testAutoClose() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/path/does/not/exist.txt")) {
            if (in == null) {
                log.debug("input stream is null");
            } else {
                try {
                    log.debug("input stream exists");
                    Properties properties = new Properties();
                    properties.load(in);
                } catch (Exception e) {
                    log.debug("cannot load resource");
                }
            }
            log.debug("after if-else");
        }
        log.debug("continued after null stream ok");
    }
}
