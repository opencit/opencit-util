/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.iso8601;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * The input patterns are based on RFC 3339 which is an Internet profile of ISO 8601.
 * However, the output pattern complies with ISO 8601 but deviates a little from RFC 3339
 * because RFC 3339 requires colon ":" between timezone offset hours and minutes, but
 * the SimpleDateFormat class does not provide an output character for exactly this so
 * we use uppercase Z for hours and minutes without a separating colon.
 * 
 * TODO:
 * Accept dates like "2013-W33" and "2013-W33-4" 
 * 
 * http://www.ietf.org/rfc/rfc3339.txt
 * http://en.wikipedia.org/wiki/ISO_8601
 * 
 * @author jbuhacoff
 */
public class Iso8601Date extends Date {
    private static final Logger log = LoggerFactory.getLogger(Iso8601Date.class);
    
    private static final String rYear = "(?:[0-9]{4})";
    private static final String rMonth = "(?:0[1-9]|1[0-2])";
    private static final String rDay = "(?:0[1-9]|[1-2][0-9]|3[0-1])";
    private static final String rHour = "(?:[0-1][0-9]|2[0-3])";
    private static final String rMinute = "(?:[0-5][0-9])";
    private static final String rSecond = "(?:[0-5][0-9]|60)"; // the 60 is for leap second
    private static final String rFraction = "(?:\\056[0-9]+)"; // octal 46 is hex 0x2e for period "." (ascii 46)
    private static final String rWeek = "(?:0[1-9]|[1-4][0-9]|5[0-2])";
    private static final String rWeekDay = "(?:[1-7])";
    private static final String rYearDay = "(?:00[1-9]|0[1-9][0-9]|[1-2][0-9][0-9]|3[0-5][0-9]|36[0-5])";
    private static final String rTimezoneWithZ = "[Zz]";
    private static final String rTimezoneWithHour = "[+-]"+rHour;
    private static final String rTimezoneWithColon = "[+-]"+rHour+":"+rMinute;
    private static final String rTimezoneWithoutColon = "[+-]"+rHour+rMinute;
    private static final String rTimezone = "(?:"+rTimezoneWithZ+"|"+rTimezoneWithHour+"|"+rTimezoneWithColon+"|"+rTimezoneWithoutColon+")";
    private static final String rTime = "(?:"+rHour+":"+rMinute+":"+rSecond+rFraction+"|"+rHour+":"+rMinute+":"+rSecond+"|"+rHour+":"+rMinute+")";
    private static final String rCompleteTime = "(?:"+rTime+rTimezone+"?)";
    private static final String rDate = rYear+"-"+rMonth+"-"+rDay;
    private static final String rDatetime = rDate+"[Tt]"+rCompleteTime;
    private static final String rDateWeek = rYear+"-W"+rWeek;
    private static final String rDateWeekDay = rYear+"-W"+rWeek+"-"+rWeekDay;
    private static final String rDateYearDay = rYear+"-"+rYearDay;
    
    private static final Pattern pTimezone = Pattern.compile(".*("+rTimezone+")$"); // anchor to end of line
    private static final Pattern pTimezoneWithZ = Pattern.compile(".*("+rTimezoneWithZ+")$");
    private static final Pattern pTimezoneWithColon = Pattern.compile(".*("+rTimezoneWithColon+")$"); // anchor to end of line
    
    private static final Pattern pDate = Pattern.compile("^(?:"+rDate+")$");
    private static final Pattern pDateWeek = Pattern.compile("^(?:"+rDateWeek+")$");
    private static final Pattern pDateWeekDay = Pattern.compile("^(?:"+rDateWeekDay+")$");
    private static final Pattern pDateYearDay = Pattern.compile("^(?:"+rDateYearDay+")$");
    private static final Pattern pDatetime = Pattern.compile("^(?:"+rDatetime+")$");
    private static final Pattern pDatetimeWithTimezone = Pattern.compile("^(?:"+rDatetime+rTimezone+")$");
//    private final Pattern datetimeTimezoneWithColon = Pattern.compile(rDatetime+"[+-]"+rTimezoneWithColon);
//    private final Pattern datetimeTimezoneWithoutColon = Pattern.compile(rDatetime+"[+-]"+rTimezoneWithoutColon);
    public static final Pattern patterns[] = new Pattern[] { pDatetime, pDate, pDateWeek, pDateWeekDay, pDateYearDay, pDatetimeWithTimezone };
    
    public static final SimpleDateFormat iso8601DateTimeInputs[] = new SimpleDateFormat[] { 
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"), 
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"), 
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ"),
        new SimpleDateFormat("yyyy-DDD'T'HH:mmZ"),
        new SimpleDateFormat("yyyy-DDD'T'HH:mm:ssZ"),
        new SimpleDateFormat("yyyy-DDD'T'HH:mm:ss.SSSZ"),
        new SimpleDateFormat("yyyy-'W'ww-F'T'HH:mmZ"),
        new SimpleDateFormat("yyyy-'W'ww-F'T'HH:mm:ssZ"),
        new SimpleDateFormat("yyyy-'W'ww-F'T'HH:mm:ss.SSSZ"),
        new SimpleDateFormat("yyyy-'W'ww'T'HH:mmZ"),
        new SimpleDateFormat("yyyy-'W'ww'T'HH:mm:ssZ"),
        new SimpleDateFormat("yyyy-'W'ww'T'HH:mm:ss.SSSZ")
        };
    public static final SimpleDateFormat iso8601DateInputs[] = new SimpleDateFormat[] { 
        new SimpleDateFormat("yyyy-MM-dd"),
        new SimpleDateFormat("yyyy-DDD"),
        new SimpleDateFormat("yyyy-'W'ww-F"),
        new SimpleDateFormat("yyyy-'W'ww")
        };
    private static final SimpleDateFormat iso8601DateOutput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"); // capital Z produces output like -0800 which is ok for iso8601 but not for rfc3339
//    private Date date;
    
    private Iso8601Date() {
        super();
    }
    
    public Iso8601Date(Date date) {
        super(date.getTime());
//        this.date = date;
    }
    
    public Iso8601Date(long milliseconds) {
        super(milliseconds);
    }
    
    @org.codehaus.jackson.annotate.JsonValue
    @com.fasterxml.jackson.annotation.JsonValue
    @Override
    public String toString() {
        return format(this);
    }
    
    /**
     * Convenience function that will take any date and represent it in
     * the ISO-8601 standard format. Useful for when you already have a
     * Date object and need to generate the ISO-8601 format without 
     * creating an intermediate Iso8601Date instance.
     * @param date
     * @return 
     */
    public static String format(Date date) {
        return iso8601DateOutput.format(date);        
    }
    
    private static final DateTimeFormatter joda = ISODateTimeFormat.dateOptionalTimeParser();
    /**
     * It is an error to pass an empty string or null value to this method.
     * 
     * @param text
     * @return 
     */
    private static Date parseText(String text) {
        return joda.parseDateTime(text).toDate();
    }
    
    /**
     * @since 0.1.2
     * @param text if null or empty string a null value will be returned
     * @return a new instance of Iso8601Date
     * @throws IllegalArgumentException if the text is not a recognized ISO8601 format; only a subset of ISO8601 is currently supported
     */
    @org.codehaus.jackson.annotate.JsonCreator
    @com.fasterxml.jackson.annotation.JsonCreator
    public static Iso8601Date valueOf(String text) {
        if( text == null || text.isEmpty()) { return null; }
        Date date = parseText(text);
        Iso8601Date isodate = new Iso8601Date(date);
        return isodate;
    }
}
