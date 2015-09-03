/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.iso8601;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class Iso8601DateTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Iso8601DateTest.class);
    

    @Test
    public void testDate() {
        assertNotNull(Iso8601Date.valueOf("2015-09-01"));
    }
    @Test
    public void testDateTimeUtc() {
        assertNotNull(Iso8601Date.valueOf("2015-09-01T12:44:03+00:00"));
    }
    @Test
    public void testDateTimeUtcZ() {
        assertNotNull(Iso8601Date.valueOf("2015-09-01T12:44:03Z"));
    }
    @Test
    public void testWeek() {
        assertNotNull(Iso8601Date.valueOf("2015-W36"));
    }
    @Test
    public void testWeekDay() {
        assertNotNull(Iso8601Date.valueOf("2015-W36-2"));
    }
    @Test
    public void testOrdinal() {
        assertNotNull(Iso8601Date.valueOf("2015-244"));
    }
}
