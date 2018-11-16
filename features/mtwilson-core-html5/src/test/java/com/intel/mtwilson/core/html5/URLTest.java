/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.core.html5;

import com.intel.kms.keystore.html5.jaxrs.Directory.DirectoryFinder;
import com.intel.kms.keystore.html5.jaxrs.Directory.DirectoryFinderFactory;
import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class URLTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(URLTest.class);

    private String normalize(String path) {
        String result = path.replaceAll("/+", "/");
        if( !result.startsWith("/") ) {
            result = "/" + result;
        }
        return result;
    }
    
    @Test
    public void testNormalize() {
        assertEquals("/public/hello/world.txt", normalize("//public///hello//world.txt"));
        assertEquals("/public/hello/world.txt", normalize("public/hello//world.txt"));
    }
    
    @Test
    public void testDirectoryFinder() {
        DirectoryFinderFactory directoryFinderFactory = new DirectoryFinderFactory("html5", "/", "/", false);
        DirectoryFinder finder = directoryFinderFactory.create("mtwilson-core-html5");
        log.debug("base path: {}", finder.getBasePath());
        log.debug("base href: {}", finder.getBaseHref());
        File test = new File(finder.getBasePath()+File.separator+"index.html");
        log.debug("test file path: {}", test.getAbsolutePath());
        assertTrue(finder.isAllowed(test));
    }
    @Test
    public void testPublicDirectoryFinder() {
        DirectoryFinderFactory directoryFinderFactory = new DirectoryFinderFactory("html5/public", "/", "/public", true);
        DirectoryFinder finder = directoryFinderFactory.create("mtwilson-core-html5");
        log.debug("base path: {}", finder.getBasePath());
        log.debug("base href: {}", finder.getBaseHref());
        File test = new File(finder.getBasePath()+File.separator+"public"+File.separator+"mtwilson-core-html5"+File.separator+"init"+File.separator+"start.js");
        log.debug("test file path: {}", test.getAbsolutePath());
        assertTrue(finder.isAllowed(test));
    }
}
