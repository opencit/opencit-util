/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.iso8601;

import static org.junit.Assert.*;
import org.junit.Test;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author jbuhacoff
 */
public class JodaTimeIso8601DateTest1 {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JodaTimeIso8601DateTest1.class);
    
    private DateTimeFormatter joda = ISODateTimeFormat.dateOptionalTimeParser();

    @Test
    public void testDate() {
        assertNotNull(joda.parseDateTime("2015-09-01"));
        log.debug("testDate: {}", joda.parseDateTime("2015-09-01").toString());
    }
    @Test
    public void testDateTimeUtc() {
        assertNotNull(joda.parseDateTime("2015-09-01T12:44:03+00:00"));
        log.debug("testDateTimeUtc: {}", joda.parseDateTime("2015-09-01T12:44:03+00:00").toString());
    }
    @Test
    public void testDateTimeUtcZ() {
        assertNotNull(joda.parseDateTime("2015-09-01T12:44:03Z"));
        log.debug("testDateTimeUtcZ: {}", joda.parseDateTime("2015-09-01T12:44:03Z").toString());
    }
    @Test
    public void testWeek() {
        assertNotNull(joda.parseDateTime("2015-W36"));
        log.debug("testWeek: {}", joda.parseDateTime("2015-W36").toString());
    }
    @Test
    public void testWeekDay() {
        assertNotNull(joda.parseDateTime("2015-W36-2"));
        log.debug("testWeekDay: {}", joda.parseDateTime("2015-W36-2").toString());
    }
    @Test
    public void testOrdinal() {
        assertNotNull(joda.parseDateTime("2015-244"));
        log.debug("testOrdinal: {}", joda.parseDateTime("2015-244").toString());
    }
}
