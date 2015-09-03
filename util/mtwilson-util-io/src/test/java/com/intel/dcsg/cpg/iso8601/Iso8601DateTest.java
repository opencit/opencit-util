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
        Iso8601Date date = Iso8601Date.valueOf("2015-09-01");
        assertNotNull(date);
        assertEquals("2015-09-01T00:00:00-0700", date.toString());
//        log.debug("testDate: {}", Iso8601Date.valueOf("2015-09-01").toString());
    }
    @Test
    public void testDateTimeUtc() {
        Iso8601Date date = Iso8601Date.valueOf("2015-09-01T12:44:03+00:00");
        assertNotNull(date);
        assertEquals("2015-09-01T05:44:03-0700", date.toString());
//        assertNotNull(Iso8601Date.valueOf("2015-09-01T12:44:03+00:00"));
//        log.debug("testDateTimeUtc: {}", Iso8601Date.valueOf("2015-09-01T12:44:03+00:00").toString());
    }
    @Test
    public void testDateTimeUtcZ() {
        Iso8601Date date = Iso8601Date.valueOf("2015-09-01T12:44:03Z");
        assertNotNull(date);
        assertEquals("2015-09-01T05:44:03-0700", date.toString());
//        assertNotNull(Iso8601Date.valueOf("2015-09-01T12:44:03Z"));
//        log.debug("testDateTimeUtcZ: {}", Iso8601Date.valueOf("2015-09-01T12:44:03Z").toString());
    }
    @Test
    public void testWeek() {
        Iso8601Date date = Iso8601Date.valueOf("2015-W36");
        assertNotNull(date);
        assertEquals("2015-08-31T00:00:00-0700", date.toString());
//        assertNotNull(Iso8601Date.valueOf("2015-W36"));
//        log.debug("testWeek: {}", Iso8601Date.valueOf("2015-W36").toString());
    }
    @Test
    public void testWeekDay() {
        Iso8601Date date = Iso8601Date.valueOf("2015-W36-2");
        assertNotNull(date);
        assertEquals("2015-09-01T00:00:00-0700", date.toString());
//        assertNotNull(Iso8601Date.valueOf("2015-W36-2"));
//        log.debug("testWeekDay: {}", Iso8601Date.valueOf("2015-W36-2").toString());
    }
    @Test
    public void testOrdinal() {
        Iso8601Date date = Iso8601Date.valueOf("2015-244");
        assertNotNull(date);
        assertEquals("2015-09-01T00:00:00-0700", date.toString());
//        assertNotNull(Iso8601Date.valueOf("2015-244"));
//        log.debug("testOrdinal: {}", Iso8601Date.valueOf("2015-244").toString());
    }
}
