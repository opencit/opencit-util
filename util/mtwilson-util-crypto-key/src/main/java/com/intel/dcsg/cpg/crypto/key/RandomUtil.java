/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key;

import com.intel.dcsg.cpg.crypto.jca.MtWilsonProvider;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class RandomUtil {

    private final static SecureRandomFactory factory = new SecureRandomFactory();

    public static SecureRandom getSecureRandom() {
        return factory.getInstance();
    }

    public static byte[] randomByteArray(int size) {
        byte[] buffer = new byte[size];
        getSecureRandom().nextBytes(buffer);
        return buffer;
    }

    public static String randomHexString(int bytes) {
        return Hex.encodeHexString(randomByteArray(bytes));
    }

    public static String randomBase64String(int bytes) {
        return Base64.encodeBase64String(randomByteArray(bytes));
    }

    public static class SecureRandomFactory {

        private final static Logger log = LoggerFactory.getLogger(SecureRandomFactory.class);
        private SecureRandom random;

        public SecureRandom getInstance() {
            if (random == null) {
                try {
                    Security.addProvider(new MtWilsonProvider());
                    random = SecureRandom.getInstance("SHA256PRNG", "MtWilson");
                } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                    log.error("Fallback required: {}", e.getMessage());
                    random = new SecureRandom();
                }
            }
            return random;
        }
    }
}
