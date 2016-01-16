/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class HashMapConfigurationTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HashMapConfigurationTest.class);
    
    public static class Fruit { public String name; public String color; public Fruit(String name, String color) { this.name = name; this.color = color; } }
    
    private MapConfiguration createConfiguration() {
        MapConfiguration c = new MapConfiguration();
        c.set("foo", "bar");
        c.set("baz", null);
        return c;
    }
    /**
2014-02-22 14:35:16,425 DEBUG [main] c.i.d.c.c.HashMapConfigurationTest [HashMapConfigurationTest.java:22] foo = bar
2014-02-22 14:35:16,443 DEBUG [main] c.i.d.c.c.HashMapConfigurationTest [HashMapConfigurationTest.java:24] apple = green
     * 
     */
    @Test
    public void testCreateConfiguration() {
        MapConfiguration c = createConfiguration();
        assertEquals("bar", c.get("foo"));
    }
    
    @Test
    public void testCreateHashMapFromConfiguration() {
        MapConfiguration c = createConfiguration();
        HashMap<String,String> map = MapConfiguration.toHashMap(c);
        for(Map.Entry<String,String> entry : map.entrySet()) {
            log.debug("key: {}  value: {}", entry.getKey(), entry.getValue());
        }
        assertEquals("bar", map.get("foo"));
        assertNull(map.get("baz"));
    }
}
