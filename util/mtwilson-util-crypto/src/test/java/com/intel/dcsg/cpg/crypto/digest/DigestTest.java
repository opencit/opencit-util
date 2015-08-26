/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.digest;

import com.intel.dcsg.cpg.crypto.digest.Digest;
import java.nio.charset.Charset;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class DigestTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigestTest.class);

    @Test
    public void testZero() {
        Digest md5zero = Digest.md5().zero();
        assertEquals("00000000000000000000000000000000", md5zero.toHex());
        Digest sha1zero = Digest.sha1().zero();
        assertEquals("0000000000000000000000000000000000000000", sha1zero.toHex());
        Digest sha256zero = Digest.sha256().zero();
        assertEquals("0000000000000000000000000000000000000000000000000000000000000000", sha256zero.toHex());
    }
    
    @Test
    public void testLength() {
        assertEquals(16, Digest.md5().length());
        assertEquals(20, Digest.sha1().length());
        assertEquals(32, Digest.sha256().length());
    }
    
    @Test
    public void testWellKnownDigests() {
        Charset utf8 = Charset.forName("UTF-8");
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", Digest.md5().digestHex("").toHex());
        assertEquals("9e107d9d372bb6826bd81d3542a419d6", Digest.md5().digest("The quick brown fox jumps over the lazy dog", utf8).toHex());
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", Digest.sha1().digestHex("").toHex());
        assertEquals("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12", Digest.sha1().digest("The quick brown fox jumps over the lazy dog", utf8).toHex());
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", Digest.sha256().digestHex("").toHex());
        assertEquals("730e109bd7a8a32b1cb9d9a09aa2325d2430587ddbc0c38bad911525", Digest.sha224().digest("The quick brown fox jumps over the lazy dog", utf8).toHex());
    }
}
