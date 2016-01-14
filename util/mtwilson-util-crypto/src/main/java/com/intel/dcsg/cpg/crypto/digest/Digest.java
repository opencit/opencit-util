/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.digest;

import com.intel.mtwilson.codec.Base64Util;
import com.intel.mtwilson.codec.HexUtil;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

/**
 * Java built-in MessageDigest API focuses on digesting messages but is not
 * convenient for passing around an object to represent an existing digest, or
 * to interpret an input value as a possible digest and validate it.
 *
 * Convenience constants MD5, SHA1, etc. are defined with Java's built-in
 * algorithm names.
 *
 * <pre>
 *
 * </pre>
 *
 * @author jbuhacoff
 */
public class Digest {

    public static final String MD5 = "MD5";
    public static final String SHA1 = "SHA-1";
    public static final String SHA224 = "SHA-224";
    public static final String SHA256 = "SHA-256";
    public static final String SHA384 = "SHA-384";
    public static final String SHA512 = "SHA-512";
    private final String algorithm; // MD5, SHA-1, SHA-256, SHA-384, SHA-512
    private byte[] data;

    /**
     *
     * @param algorithm the Java algorithm name, to be passed to MessageDigest
     * if necessary
     * @param data
     */
    protected Digest(String algorithm, byte[] value) {
        this.algorithm = algorithm;
        this.data = value;
    }

    /**
     *
     * @return the digest algorithm name associated with the digest data
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     *
     * @return a copy of the digest bytes
     */
    public byte[] getBytes() {
        return Arrays.copyOf(data, data.length);
    }

    public String toHex() {
        return Hex.encodeHexString(data);
    }

    public String toBase64() {
        return Base64.encodeBase64String(data);
    }

    @Override
    public String toString() {
        return String.format("[%s: %s]", algorithm, toHex());
    }

    public Digest extend(byte[] more) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(data);
            md.update(more);
            return new Digest(algorithm, md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedAlgorithmException(algorithm, e);
        }

    }

    public Digest copy() {
        return new Digest(algorithm, Arrays.copyOf(data, data.length));
    }

    public static DigestBuilder algorithm(String algorithm) {
        return new DigestBuilder(algorithm);
    }

    public static DigestBuilder md5() {
        return new DigestBuilder(MD5);
    }

    public static DigestBuilder sha1() {
        return new DigestBuilder(SHA1);
    }

    public static DigestBuilder sha224() {
        return new DigestBuilder(SHA224);
    }

    public static DigestBuilder sha256() {
        return new DigestBuilder(SHA256);
    }

    public static DigestBuilder sha384() {
        return new DigestBuilder(SHA384);
    }

    public static DigestBuilder sha512() {
        return new DigestBuilder(SHA512);
    }

    public static class DigestBuilder {

        private final String algorithm;

        protected DigestBuilder(String algorithm) {
            this.algorithm = normalizeAlgorithm(algorithm);
        }

        private String normalizeAlgorithm(String algorithmName) {
            String normalized = DigestUtil.getJavaAlgorithmName(algorithmName);
            if (normalized != null) {
                return normalized;
            }
            return algorithmName;
        }

        public boolean isValid(byte[] digestBytes) {
            if (digestBytes == null || digestBytes.length != length()) {
                return false;
            }
            return true;
        }

        public boolean isValidHex(String digestHex) {
            if (digestHex == null) {
                return false;
            }
            digestHex = HexUtil.trim(digestHex);
            return (digestHex.length() == length() * 2) && HexUtil.isHex(digestHex);
        }

        public boolean isValidBase64(String digestBase64) {
            if (digestBase64 == null) {
                return false;
            }
            digestBase64 = Base64Util.trim(digestBase64);
            return (digestBase64.length() == Math.round(4 * Math.ceil(1.0 * length() / 3))) && Base64.isBase64(digestBase64);
        }

        /**
         *
         * @return the length in bytes of a digest of the specified algorithm;
         * for example MD5 length is 16, SHA1 length is 20
         */
        public int length() {
            try {
                MessageDigest hash = MessageDigest.getInstance(algorithm); // throws NoSuchAlgorithmException; example of algorithm is "MD5", "SHA-1", "SHA-256"
                return hash.digest(new byte[0]).length;
            } catch (NoSuchAlgorithmException e) {
                throw new UnsupportedAlgorithmException(algorithm, e);
            }
        }

        public Digest zero() {
            byte[] zeros = new byte[length()];
            Arrays.fill(zeros, (byte) 0x00);
            return new Digest(algorithm, zeros);
        }

        public Digest value(byte[] value) {
            int digestLength = length();
            if (value.length != digestLength) {
                throw new DigestLengthException(algorithm, digestLength, value.length);
            }
            return new Digest(algorithm, value);
        }

        public Digest valueHex(String valueHex) {
            return value(HexUtil.toByteArray(HexUtil.normalize(valueHex)));
        }

        public Digest valueBase64(String valueBase64) {
            return value(Base64Util.toByteArray(Base64Util.normalize(valueBase64)));
        }

        /**
         * To digest the string "hello world", call
         * {@code digest("hello world".getBytes("UTF-8"))}
         *
         * @param message to digest; can be any length
         * @return
         */
        public Digest digest(byte[] message) {
            try {
                MessageDigest hash = MessageDigest.getInstance(algorithm); // throws NoSuchAlgorithmException; example of algorithm is "MD5", "SHA-1", "SHA-256"
                return new Digest(algorithm, hash.digest(message));
            } catch (NoSuchAlgorithmException e) {
                throw new UnsupportedAlgorithmException(algorithm, e);
            }
        }

        public Digest digest(String message, Charset charset) {
            return digest(message.getBytes(charset));
        }

        /**
         * Reads the InputStream fully and computes a digest of the entire
         * stream content. Closes the InputSream on successful completion.
         * 
         * Use this method when you need to digest a large object but don't
         * need to do any other processing with it.  If you need to process
         * the stream in addition to digesting it, use DigestInputStream
         * directly.
         * 
         * @param in
         * @return
         * @throws IOException 
         */
        public Digest digest(InputStream in) throws IOException {
            try {
                MessageDigest hash = MessageDigest.getInstance(algorithm); // throws NoSuchAlgorithmException; example of algorithm is "MD5", "SHA-1", "SHA-256"
                try (DigestInputStream digest = new DigestInputStream(in, hash)) {
                    int bytesReceived = 0;
                    byte[] buffer = new byte[1024];
                    while (bytesReceived != -1) {
                        bytesReceived = digest.read(buffer);
                    }
                    return new Digest(algorithm, hash.digest());
                }
            } catch (NoSuchAlgorithmException e) {
                throw new UnsupportedAlgorithmException(algorithm, e);
            }
        }

        /**
         *
         * @param messageHex hex representation of the bytes to digest; the
         * input is first converted to bytes ("00" is byte 0x00) then digested
         * @return
         */
        public Digest digestHex(String messageHex) {
            return digest(HexUtil.toByteArray(HexUtil.normalize(messageHex)));
        }

        /**
         *
         * @param messageBase64 base64 representation of the bytes to digest;
         * the input is first converted to bytes then digested
         * @return
         */
        public Digest digestBase64(String messageBase64) {
            return digest(Base64Util.toByteArray(Base64Util.normalize(messageBase64)));
        }
    }
}
