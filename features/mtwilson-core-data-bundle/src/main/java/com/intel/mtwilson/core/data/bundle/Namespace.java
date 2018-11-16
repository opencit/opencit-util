/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.core.data.bundle;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author jbuhacoff
 */
public interface Namespace extends Iterable<String> {
    
    /**
     * 
     * @return the namespace
     */
    String getName();
    
    /*
    @Override
    Iterator<String> iterator();
    */
    
    /**
     * @return paths representing all the file entries in the namespace at any depth; empty directories are not represented
     */
    Collection<String> list();

    /**
     * 
     * @param path true if the namespace contains an entry at this path
     * @return 
     */
    boolean contains(String path);


    /**
     * @param path
     * @return raw content bytes at this path
     * @throws FileNotFoundException if path is not found in bundle
     */
    byte[] get(String path) throws FileNotFoundException;

    void set(String path, byte[] content);

    /**
     * 
     * @return true if the bundle allows new entries, editing entries, or removing entries
     */
    boolean isEditable();
        
}
