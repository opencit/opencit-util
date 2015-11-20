/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.http;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jbuhacoff
 */
public class Query {
    protected LinkedHashMap<String,ArrayList<String>> map;
    
    public Query() {
        map = new LinkedHashMap<String,ArrayList<String>>();
    }
    
    public Query(Map<String,String[]> map) {
        this();
        for(String key : map.keySet()) {
            ArrayList<String> copy = new ArrayList<String>();
            copy.addAll(Arrays.asList(map.get(key)));
            this.map.put(key, copy);
        }
    }
    
    public Set<String> keySet() {
        return Collections.unmodifiableSet(map.keySet());
    }
    
    public List<String> getAll(String name) {
        ArrayList<String> values = map.get(name);
        if( values == null ) {
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(values);
    }
    
    public String getFirst(String name) {
        List<String> values = getAll(name);
        if( values.isEmpty() ) {
            return null;
        }
        return values.get(0);
    }
    
    @Override
    public String toString() {
        ArrayList<String> pairs = new ArrayList<String>();
        for(String key : map.keySet()) {
            List<String> values = map.get(key);
            if( values == null || values.isEmpty() ) { continue; }
            for(String value : values) {
                pairs.add(String.format("%s=%s", escape(key), escape(value)));
            }
        }
        return StringUtils.join(pairs, "&");
    }
    
    protected String escape(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        }
        catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e); // java runtime guarantees availability of utf-8 so this will never happen
        }
    }
    
    public static Map<String, List<String>> parse(URL url) throws UnsupportedEncodingException {
        return parse(url.getQuery());
    }
    public static Map<String, List<String>> parse(String query) throws UnsupportedEncodingException {
      final Map<String, List<String>> parameters = new LinkedHashMap<>();
      final String[] pairs = query.split("&");
      for (String pair : pairs) {
        final int separator = pair.indexOf("=");
        final String key = separator > 0 ? URLDecoder.decode(pair.substring(0, separator), "UTF-8") : pair;
        if (!parameters.containsKey(key)) {
          parameters.put(key, new LinkedList<String>());
        }
        final String value = separator > 0 && pair.length() > separator + 1 ? URLDecoder.decode(pair.substring(separator + 1), "UTF-8") : null;
        parameters.get(key).add(value);
      }
      return parameters;
    }    
}
