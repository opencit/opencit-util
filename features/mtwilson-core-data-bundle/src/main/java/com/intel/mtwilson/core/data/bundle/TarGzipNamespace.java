/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.core.data.bundle;

import com.intel.mtwilson.pipe.Filter;
import com.intel.mtwilson.pipe.FilterIterator;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author jbuhacoff
 */
public class TarGzipNamespace implements Namespace {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TarGzipNamespace.class);

    private final String name;
    private final TarGzipBundle bundle;

    public TarGzipNamespace(String name, TarGzipBundle bundle) {
        this.name = name;
        this.bundle = bundle;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Collection<String> list() {
        ArrayList<String> list = new ArrayList<>();
        Iterator<String> it = iterator();
        while(it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }
    
    private static class StartWithFilter implements Filter<String> {
        private String prefix;

        public StartWithFilter(String prefix) {
            this.prefix = prefix;
        }
        
        @Override
        public boolean accept(String item) {
            return item != null && item.startsWith(prefix);
        }
        
    }

    @Override
    public Iterator<String> iterator() {
        FilterIterator<String> it = new FilterIterator<>(new StartWithFilter(name+"/"), bundle.iterator());
        return it;
    }

    @Override
    public boolean contains(String path) {
        return bundle.contains(name+"/"+path);
    }

    @Override
    public byte[] get(String path) throws FileNotFoundException {
        return bundle.getBytes(name+"/"+path);
    }

    @Override
    public void set(String path, byte[] content) {
        bundle.set(name+"/"+path, content);
    }

    @Override
    public boolean isEditable() {
        return bundle.isEditable();
    }
    
    public boolean isClosed() {
        return bundle.isClosed();
    }
}
