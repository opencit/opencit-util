/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.http;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author jbuhacoff
 */
public class QueryTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QueryTest.class);
    
    @Test
    public void testQueryString() {
        MutableQuery query = new MutableQuery();
        query.add("key1", "value1");
        query.add("key2", "value2a");
        query.add("key2", "value2b");
        assertEquals("key1=value1&key2=value2a&key2=value2b", query.toString());
        query.removeAll("key2");
        assertEquals("key1=value1", query.toString());
        query.clear();
        assertEquals("", query.toString());
    }
    
    @Test
    public void testQueryWithEncodedValues() {
        MutableQuery query = new MutableQuery();
        query.add("key1", "value with space");
        query.add("key2", ".-*_");
        query.add("key3", "ü@foo-bar");
        query.add("key four", "");
        assertEquals("key1=value+with+space&key2=.-*_&key3=%C3%BC%40foo-bar&key+four=", query.toString());
    }
    
    @Test
    public void testEmptyQuery() {
        MutableQuery query = new MutableQuery();
        assertEquals("", query.toString());
    }
    
    @Test
    public void testParseQuery() throws UnsupportedEncodingException, MalformedURLException {
        Map<String,List<String>> parameters = Query.parse(new URL("http://localhost/path?key1=value1&key2=value2a&key2=value2b"));
        assertEquals("value1", parameters.get("key1").get(0));
        assertEquals("value2a", parameters.get("key2").get(0));
        assertEquals("value2b", parameters.get("key2").get(1));
    }
    
    private void logParameters(Map<String,List<String>> parameters) {
        for( String key : parameters.keySet() ) {
            log.debug("key: {}", key);
            List<String> values = parameters.get(key);
            if( values == null ) { log.debug("values null"); }
            else {
                for( String value : values ) {
                    log.debug("value: {}", value);
                }
            }
        }
    }
    
    @Test
    public void testParseEmptyQuery() throws UnsupportedEncodingException, MalformedURLException {
        Map<String,List<String>> parameters = Query.parse("");
        assertNotNull(parameters);
        logParameters(parameters);
    }
    
    @Test
    public void testParseEmptyQueryWithAmp() throws UnsupportedEncodingException, MalformedURLException {
        Map<String,List<String>> parameters = Query.parse("&");
        assertNotNull(parameters);
        logParameters(parameters);
    }

    @Test
    public void testParseEmptyQueryWithSemicolon() throws UnsupportedEncodingException, MalformedURLException {
        Map<String,List<String>> parameters = Query.parse(";");
        assertNotNull(parameters);
        logParameters(parameters);
    }

    @Test
    public void testParseEmptyQueryWithQuestion() throws UnsupportedEncodingException, MalformedURLException {
        Map<String,List<String>> parameters = Query.parse("?");
        assertNotNull(parameters);
        logParameters(parameters);
    }

}
