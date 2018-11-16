/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.ssh;

import com.intel.dcsg.cpg.io.ByteArray;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

/**
 * SSH format for encoding an RSA public key is specified in RFC 4253
 * Public Key Algorithms
 * https://tools.ietf.org/html/rfc4253#section-6.6
 * 
 * <pre>
 * The "ssh-rsa" key format has the following specific encoding:
 *
 *    string    "ssh-rsa"
 *    mpint     e
 *    mpint     n
 *</pre>
 * 
 * The types "string" and "mpint" are specified in RFC 4251 Data Type
 * Representations Used in the SSH Protocols
 * http://tools.ietf.org/html/rfc4251#section-5
 * 
 * Definition of string specifies a uint32 length stored before the string
 * data, and this is also done for mpint even though the definition doesn't
 * mention it.
 * 
 *
 * @author jbuhacoff
 */
public class SshUtils {
    
    /**
     * Implementation of data encodings specified in RFC 4251 section 5
     * http://tools.ietf.org/html/rfc4251#section-5
     * 
     */
    public static class Rfc4251 {
        private static final Charset UTF8 = Charset.forName("UTF-8");
        
        // UTILITIES 
        
        /**
         * 
         * @param data
         * @return uint32 encoded length of the input data
         */
        public static byte[] length(byte[] data) {
            return uint32(data.length);
        }

        /**
         * 
         * @param data to pad
         * @param minLength the minimum length the data should be; if data length is less than this it will be prefixed with enough zeros so it becomes this minimum length
         * @return padded data if smaller than minLength, or original data if already minLength or larger
         */
        public static byte[] prefixZeroPadding(byte[] data, int minLength) {
            if( data.length < minLength ) {
                byte[] zeroPadding = new byte[minLength-data.length];
                Arrays.fill( zeroPadding, (byte)0x00 ); // local variables are not initialized by default: https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html
                return ByteArray.concat(zeroPadding, data);
            }
            return data;
        }
        
        // SECTION 5 IMPLEMENTATION

        public static byte[] uint32(int n) {
            return prefixZeroPadding(BigInteger.valueOf(n).toByteArray(), 4);
        }
        
        public static byte[] mpint(BigInteger n) {        
            byte[] bytes = n.toByteArray(); //  // BigInteger inserts leading zero for positive numbers, which is what we want
            return ByteArray.concat(length(bytes), bytes);
        }

        public static byte[] string(String s) {
            byte[] bytes = s.getBytes(UTF8);
            return ByteArray.concat(length(bytes), bytes);
        }
        
    }
    
    /**
     * 
     * @param publicKey
     * @return 
     */
    public static byte[] encodeSshRsaPublicKey(RSAPublicKey publicKey) {
        return ByteArray.concat(Rfc4251.string("ssh-rsa"), Rfc4251.mpint(publicKey.getPublicExponent()), Rfc4251.mpint(publicKey.getModulus()));
    }
    
}
