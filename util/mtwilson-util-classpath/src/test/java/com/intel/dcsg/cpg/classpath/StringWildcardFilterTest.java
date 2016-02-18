/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.classpath;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class StringWildcardFilterTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringWildcardFilterTest.class);

    @Test
    public void testEquals() {
        StringWildcardFilter filter = new StringWildcardFilter("foo");
        assertTrue(filter.accept("foo"));
        assertFalse(filter.accept("xfoo"));
        assertFalse(filter.accept("xfoox"));
        assertFalse(filter.accept("foox"));
        assertFalse(filter.accept("foo "));
        assertFalse(filter.accept(" foo "));
        assertFalse(filter.accept(" foo"));
    }
    
    @Test
    public void testEndsWith() {
        StringWildcardFilter filter = new StringWildcardFilter("*foo");
        assertTrue(filter.accept("foo"));
        assertTrue(filter.accept("xfoo"));
        assertFalse(filter.accept("xfoox"));
        assertFalse(filter.accept("foox"));
        assertFalse(filter.accept("foo "));
        assertFalse(filter.accept(" foo "));
        assertTrue(filter.accept(" foo"));
    }

    @Test
    public void testStartsWith() {
        StringWildcardFilter filter = new StringWildcardFilter("foo*");
        assertTrue(filter.accept("foo"));
        assertFalse(filter.accept("xfoo"));
        assertFalse(filter.accept("xfoox"));
        assertTrue(filter.accept("foox"));
        assertTrue(filter.accept("foo "));
        assertFalse(filter.accept(" foo "));
        assertFalse(filter.accept(" foo"));
    }

    @Test
    public void testContains() {
        StringWildcardFilter filter = new StringWildcardFilter("*foo*");
        assertTrue(filter.accept("foo"));
        assertTrue(filter.accept("xfoo"));
        assertTrue(filter.accept("xfoox"));
        assertTrue(filter.accept("foox"));
        assertTrue(filter.accept("foo "));
        assertTrue(filter.accept(" foo "));
        assertTrue(filter.accept(" foo"));
    }
    
}
