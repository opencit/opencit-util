/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.core.data.bundle;

import com.intel.mtwilson.util.archive.TarGzipBuilder;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

/**
 * Note: this class does not support the presence of non-regular files and
 * directories in the .tgz archive -- block devices, links, etc. may cause
 * faults.
 *
 * @author jbuhacoff
 */
public class TarGzipBundle implements Bundle {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TarGzipBundle.class);
    private Charset charset = Charset.forName("UTF-8");
    private final HashMap<String, byte[]> bundle = new HashMap<>();
    private boolean closed = false;

    public TarGzipBundle() {
    }

    /**
     * Reads the content of the given input stream (which should be a .tgz
     * stream) and adds them to the bundle. You can call this multiple times in
     * order to continue adding more content to the bundle.
     *
     * @param in
     * @throws IOException
     */
    @Override
    public void read(InputStream in) throws IOException {
        if (in == null) {
            throw new NullPointerException();
        }
        try (GzipCompressorInputStream gzip = new GzipCompressorInputStream(in)) {
            try (TarArchiveInputStream tar = new TarArchiveInputStream(gzip)) {
                TarArchiveEntry entry;
                while ((entry = tar.getNextTarEntry()) != null) {
                    log.debug("name = {}", entry.getName());
                    if (!tar.canReadEntryData(entry)) {
                        log.warn("Cannot read entry data for: {}", entry.getName());
                        continue;
                    }
                    if (entry.isFile()) {
                        if (entry.getSize() > Integer.MAX_VALUE) {
                            log.warn("Cannot read entry: max 2GB, actual size {} bytes", entry.getSize());
                            continue;
                        }
                        int entrySize = (int) entry.getSize();
//                        log.debug("entry size: {}", entrySize);
//                        int blockSize = tar.getRecordSize();
//                        log.debug("record size: {}", blockSize);
                        byte[] content = new byte[entrySize];
                        int offset = 0;
                        while (offset < entrySize) {
                            int read = tar.read(content, offset, entrySize - offset);
                            if (read > -1) {
                                offset += read;
                            } else {
                                break;
                            }
                        }
                        if (content.length != entrySize) {
                            log.error("error reading record");
                        }
                        String path = entry.getName();
                        bundle.put(path, content);
//                        }
                    }
                }
            }
        }
    }

    @Override
    public void write(OutputStream out) throws IOException {
        if (out == null) {
            throw new NullPointerException();
        }
        try (TarGzipBuilder builder = new TarGzipBuilder(out)) {
            for (String entry : bundle.keySet()) {
                builder.add(entry, bundle.get(entry));
            }
        }
    }

    @Override
    public Iterator<String> iterator() {
        return list().iterator();
    }

    /**
     *
     * @return a read-only set of all paths in the bundle
     */
    @Override
    public Collection<String> list() {
        return Collections.unmodifiableSet(bundle.keySet());
    }

    /**
     *
     * @return a read-only set of all entries in the bundle
     */
    @Override
    public Iterable<Entry> entries() {
        ArrayList<Entry> list = new ArrayList<>();
        for (Map.Entry<String, byte[]> entry : bundle.entrySet()) {
            list.add(new Entry(entry.getKey(), entry.getValue()));
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public boolean contains(String path) {
        return bundle.containsKey(path);
    }

    @Override
    public byte[] getBytes(String path) throws FileNotFoundException {
        if (!bundle.containsKey(path)) {
            throw new FileNotFoundException(path);
        }
        return bundle.get(path);
    }

    @Override
    public String getString(String path) throws FileNotFoundException {
        if (!bundle.containsKey(path)) {
            throw new FileNotFoundException(path);
        }
        return new String(bundle.get(path), charset);
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public void set(String path, byte[] content) {
        bundle.put(path, content);
    }

    @Override
    public void set(String path, String content) {
        bundle.put(path, content.getBytes(charset));
    }

    @Override
    public Entry getEntry(String path) {
        if (!bundle.containsKey(path)) {
            return null;
        }
        return new Entry(path, bundle.get(path));
    }

    @Override
    public void setEntry(Entry entry) {
        bundle.put(entry.getPath(), entry.getContent());
    }

    /**
     * Deletes any temporary files created during read().
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        closed = true;
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public Namespace namespace(String namespace) {
        return new TarGzipNamespace(namespace, this);
    }
}
