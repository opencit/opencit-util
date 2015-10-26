/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.core.data.bundle;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collection;

/**
 *
 * @author jbuhacoff
 */
public interface Bundle extends Closeable, Iterable<String> {
    
    /**
     * Reads the bundle from the provided stream. The bundle may need
     * to use temporary local disk storage for any large objects 
     * included in the bundle in order to provide random-access to them
     * without keeping everything in memory. These temporary files
     * would be deleted when the close() method is called.
     * @param in 
     */
    void read(InputStream in) throws IOException;
    
    /**
     * Writes the bundle to the provided stream. The bundle may need
     * to access temporary files written during a prior read(),  so 
     * this must be done while the bundle is "open". 
     * @param out 
     */
    void write(OutputStream out) throws IOException;
    
    
    
    /**
     * @return paths representing all the entries in the bundle, including their namespaces
     */
    Collection<String> list();
    
    Namespace namespace(String namespace);
    
    /**
     * 
     * @return all the entries in the bundle
     */
    Iterable<Entry> entries();
    
    /**
     * If writing strings directly to the bundle, they will be encoded and
     * decoded using this character set. Default UTF-8.
     * @return 
     */
    Charset getCharset();
    
    /**
     * 
     * @param path
     * @return true if the bundle contains an entry at this path
     */
    boolean contains(String path);
    
    /**
     * @param path
     * @return raw content bytes at this path
     * @throws FileNotFoundException if path is not found in bundle
     */
    byte[] getBytes(String path) throws FileNotFoundException;
    
    /**
     * 
     * @param path
     * @return string at this path, decoded using charset returned by getCharset()
     * @throws FileNotFoundException if path is not found in bundle
     */
    String getString(String path) throws FileNotFoundException;
    
    /**
     * 
     * @return true if the bundle allows new entries, editing entries, or removing entries
     */
    boolean isEditable();
    
    void set(String path, byte[] content);
    
    void set(String path, String content);
    
    Entry getEntry(String path);
    void setEntry(Entry entry);    
}
