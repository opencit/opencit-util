/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.jca;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SecureRandomSpi;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.prng.DigestRandomGenerator;
import org.bouncycastle.crypto.prng.ThreadedSeedGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses BouncyCastle's DigestRandomGenerator with SHA256, and either Java's
 * NativePRNG (on Linux) or BouncyCastle's ThreadedSeedGenerator (where
 * NativePRNG is not available).
 *
 * See also: MtWilsonProvider
 *
 * @author jbuhacoff
 */
public class BouncyCastleSecureRandomGeneratorSHA256DigestSpi extends SecureRandomSpi {

    private final static Logger log = LoggerFactory.getLogger(BouncyCastleSecureRandomGeneratorSHA256DigestSpi.class);
    private final static DigestRandomGenerator random = new DigestRandomGenerator(new SHA256Digest());
    private final static int SEED_LENGTH_BYTES = 12;
    private final static long MAX = Integer.MAX_VALUE / 2; // number of random bytes before we force a re-seed
    private long current; // number of random bytes already generated using current seed
    private final Generator generator;

    public BouncyCastleSecureRandomGeneratorSHA256DigestSpi() {
        super();
        SecureRandom nativePRNG = null;
        try {
            nativePRNG = SecureRandom.getInstance("NativePRNG");
        } catch (NoSuchAlgorithmException e) {
            log.debug("Fallback required: {}", e.getMessage());
        }
        if( nativePRNG != null ) {
            generator = new SecureRandomGenerator(nativePRNG);
        }
        else {
            generator = new BouncyCastleThreadedSeedGenerator();
        }
        // force seeding before we generate first random bytes
        current = MAX;
    }

    private static interface Generator {

        public byte[] generateBytes(int count);
    }

    private static class SecureRandomGenerator implements Generator {

        private final SecureRandom delegate;

        public SecureRandomGenerator(SecureRandom delegate) {
            this.delegate = delegate;
        }

        @Override
        public byte[] generateBytes(int count) {
            byte[] bytes = new byte[count];
            delegate.nextBytes(bytes);
            return bytes;
        }
    }

    private static class BouncyCastleThreadedSeedGenerator implements Generator {

        private final ThreadedSeedGenerator delegate;

        public BouncyCastleThreadedSeedGenerator() {
            this.delegate = new ThreadedSeedGenerator();
        }

        @Override
        public byte[] generateBytes(int count) {
            return delegate.generateSeed(count, false);
        }
    }

    @Override
    protected final void engineSetSeed(byte[] seed) {
        random.addSeedMaterial(seed);
        current = 0;
    }

    @Override
    protected final void engineNextBytes(byte[] bytes) {
        if (current >= MAX || current < 0) {
            engineSetSeed(engineGenerateSeed(SEED_LENGTH_BYTES));
        }
        random.nextBytes(bytes);
        current += bytes.length;
    }

    @Override
    protected final byte[] engineGenerateSeed(int numBytes) {
        return generator.generateBytes(numBytes);
    }

}
