/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.digest;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author jbuhacoff
 */
public class DigestInputStreamTest {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigestInputStreamTest.class);
    
    @Test
    public void testDigestInputStream() throws NoSuchAlgorithmException, IOException {
        InputStream in = getClass().getResourceAsStream("/helloworld.zip");
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (DigestInputStream digest = new DigestInputStream(in, md)) {
            int bytesReceived = 0;
            byte[] buffer = new byte[1024];
            try {
                while (bytesReceived != -1) {
                    bytesReceived = digest.read(buffer);
                }
                byte[] hash = md.digest();
                String hex1 = Hex.encodeHexString(hash);
                log.debug("digest: {}", hex1);
                
                // now compare to just reading all the bytes in and using the digest method that way
                InputStream in2 = getClass().getResourceAsStream("/helloworld.zip");
                byte[] data = IOUtils.toByteArray(in2);
                String hex2 = Digest.sha256().digest(data).toHex();
                assertEquals(hex2, hex1);
                
                // now compare that to what we get from Digest class:
                InputStream in3 = getClass().getResourceAsStream("/helloworld.zip");
                String hex3 = Digest.sha256().digest(in3).toHex();
                assertEquals(hex3, hex1);
                log.debug("digest: {}", hex3);
                
            } catch (IOException e) {
                log.error("Failed to read input stream", e);
            }
        }
    }
}
