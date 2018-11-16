/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class PropertiesConfigurationTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PropertiesConfigurationTest.class);
    
    @Test
    public void testCreatePropertiesFromConfiguration() {
        PropertiesConfiguration c = new PropertiesConfiguration();
        c.set("foo", "bar");
        c.set("baz", null);
        
        Properties properties = PropertiesConfiguration.toProperties(c);
        for(Object key : properties.keySet()) {
            log.debug("key: {}  value: {}", (String)key, properties.getProperty((String)key));
        }
        assertEquals("bar", properties.getProperty("foo"));
        assertNull(properties.getProperty("baz"));
        
    }
}
