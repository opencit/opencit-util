/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.crypto.jca.MtWilsonProvider;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.junit.Test;
import org.bouncycastle.crypto.prng.DigestRandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class SecureRandomTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Test
    public void testBC() {
        DigestRandomGenerator random = new DigestRandomGenerator(new SHA256Digest());
        byte[] bytes = new byte[4];
        random.nextBytes(bytes);
        log.debug("random: {}", Hex.encodeHexString(bytes));
    }

    // fails on windows because NativePRNG uses /dev/random and /dev/urandom which are not available on windows
    /*
    @Test
    public void testJavaSecureRandomSha1() throws NoSuchAlgorithmException, NoSuchProviderException {
        SecureRandom random = SecureRandom.getInstance("NativePRNG");
        byte[] bytes = new byte[4];
        random.nextBytes(bytes);
        log.debug("random: {}", Hex.encodeHexString(bytes));
    }
    */
//        Security.addProvider(new BouncyCastleProvider());
    
    @Test
    public void testMtWilsonSecureRandom() throws NoSuchAlgorithmException, NoSuchProviderException {
        //SecureRandom random = RandomUtil.getSecureRandom();
        Security.addProvider(new MtWilsonProvider());
        SecureRandom random = SecureRandom.getInstance("SHA256PRNG", "MtWilson");
        byte[] bytes = new byte[4];
        random.nextBytes(bytes);
        log.debug("random: {}", Hex.encodeHexString(bytes));
    }

}
