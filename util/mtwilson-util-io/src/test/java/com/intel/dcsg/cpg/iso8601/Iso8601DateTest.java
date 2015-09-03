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
        log.debug("testDate: {}", Iso8601Date.valueOf("2015-09-01").toString());
    }
    @Test
    public void testDateTimeUtc() {
        assertNotNull(Iso8601Date.valueOf("2015-09-01T12:44:03+00:00"));
        log.debug("testDateTimeUtc: {}", Iso8601Date.valueOf("2015-09-01T12:44:03+00:00").toString());
    }
    @Test
    public void testDateTimeUtcZ() {
        assertNotNull(Iso8601Date.valueOf("2015-09-01T12:44:03Z"));
        log.debug("testDateTimeUtcZ: {}", Iso8601Date.valueOf("2015-09-01T12:44:03Z").toString());
    }
    @Test
    public void testWeek() {
        assertNotNull(Iso8601Date.valueOf("2015-W36"));
        log.debug("testWeek: {}", Iso8601Date.valueOf("2015-W36").toString());
    }
    @Test
    public void testWeekDay() {
        assertNotNull(Iso8601Date.valueOf("2015-W36-2"));
        log.debug("testWeekDay: {}", Iso8601Date.valueOf("2015-W36-2").toString());
    }
    @Test
    public void testOrdinal() {
        assertNotNull(Iso8601Date.valueOf("2015-244"));
        log.debug("testOrdinal: {}", Iso8601Date.valueOf("2015-244").toString());
    }
}
