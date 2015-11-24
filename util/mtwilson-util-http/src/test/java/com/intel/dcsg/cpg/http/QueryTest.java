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
}
