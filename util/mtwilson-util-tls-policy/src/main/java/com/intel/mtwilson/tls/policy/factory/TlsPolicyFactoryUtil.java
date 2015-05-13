/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.factory;

import com.intel.mtwilson.codec.Base64Codec;
import com.intel.mtwilson.codec.Base64Util;
import com.intel.mtwilson.codec.ByteArrayCodec;
import com.intel.mtwilson.codec.HexCodec;
import com.intel.mtwilson.codec.HexUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.io.Resource;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
//import com.intel.mtwilson.My;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.TlsProtection;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyFactoryUtil {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TlsPolicyFactoryUtil.class);

    /**
     * Utility function to detect if the sample is base64-encoded or hex-encoded
     * and return a new instance of the appropriate codec. If the sample
     * encoding cannot be detected, this method will return null.
     *
     * @param sample of data either base64-encoded or hex-encoded
     * @return a new codec instance or null if the encoding is not recognized
     */
    public static String guessEncodingForData(String sample) {
        log.debug("guessEncodingForData: {}", sample);
        if( sample == null ) { return null; }
//        String printable = sample.replaceAll("[^\\p{Print}]", "");
        String printable = sample.replaceAll("["+HexUtil.NON_HEX+"&&"+Base64Util.NON_BASE64+"]", "");
        log.debug("guessEncodingForData printable: {}", printable);
        String hex = HexUtil.trim(printable);
        if (HexUtil.isHex(hex)) {
            log.debug("guessEncodingForData hex: {}", hex);
            return "hex";
        }
        String base64 = Base64Util.trim(printable);
        if (Base64Util.isBase64(base64)) {
            log.debug("guessEncodingForData base64: {}", base64);
            return "base64";
        }
        log.debug("guessEncodingForData failed");
        return null;
    }

    /**
     * Utility function to instantiate a codec by name
     *
     * @param encoding "base64" or "hex" or null
     * @return new codec instance or null if the encoding name is null or is not
     * recognized
     */
    public static ByteArrayCodec getCodecByName(String encoding) {
        if (encoding == null) {
            return null;
        }
        switch (encoding) {
            case "base64": {
                Base64Codec codec = new Base64Codec();
                codec.setNormalizeInput(true);
                return codec;
            }
            case "hex": {
                HexCodec codec = new HexCodec();
                codec.setNormalizeInput(true);
                return codec;
            }
            default:
                return null;
        }
    }

    public static String guessAlgorithmForDigest(byte[] hash) {
        if (hash.length == 16) {
            return "MD5";
        }
        if (hash.length == 20) {
            return "SHA-1";
        }
        if (hash.length == 32) {
            return "SHA-256";
        }
        if (hash.length == 48) {
            return "SHA-384";
        }
        if (hash.length == 64) {
            return "SHA-512";
        }
        return null;
    }

    /**
     * Utility function to get a sample item from a collection
     *
     * @param collection
     * @return the first item from the collection, or null if the collection is
     * empty
     */
    public static String getFirst(Collection<String> collection) {
        Iterator<String> it = collection.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    public static TlsProtection getAllTlsProtection() {
        TlsProtection tlsProtection = new TlsProtection();
        tlsProtection.integrity = true;
        tlsProtection.encryption = true;
        tlsProtection.authentication = true;
        tlsProtection.forwardSecrecy = true;
        return tlsProtection;
    }

    public static List<X509Certificate> getTrustedTlsCertificatesFromSimpleKeystore(SimpleKeystore tlsKeystore) {
        ArrayList<X509Certificate> list = new ArrayList<>();
        if (tlsKeystore != null) {
            try {
                X509Certificate[] cacerts = tlsKeystore.getTrustedCertificates(SimpleKeystore.CA);
                list.addAll(Arrays.asList(cacerts));
                X509Certificate[] sslcerts = tlsKeystore.getTrustedCertificates(SimpleKeystore.SSL);
                list.addAll(Arrays.asList(sslcerts));
            } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateEncodingException e) {
                log.warn("Cannot load trusted TLS certificates from Mt Wilson 1.x keystore", e);
            }
        }
        return list;
    }
	
    /**
     * Requires extensions to find available tls policy creators that may handle
     * the given descriptor.
     * 
     * @param tlsPolicyDescriptor comprised of policy type and policy-specific data
     * @return 
     */
    public static TlsPolicy createTlsPolicy(TlsPolicyDescriptor tlsPolicyDescriptor) {
        String policyType = tlsPolicyDescriptor.getPolicyType();
        log.debug("Trying to read TlsPolicy type {}", policyType);
        List<TlsPolicyCreator> creators = Extensions.findAll(TlsPolicyCreator.class);
        for(TlsPolicyCreator creator : creators ) {
            try {
                log.debug("Trying to read TlsPolicy with {}", creator.getClass().getName());
                TlsPolicy tlsPolicy = creator.createTlsPolicy(tlsPolicyDescriptor); // throws IllegalArgumentException
                if( tlsPolicy == null ) {
                    continue; // creator does not support the given descriptor
                }
                log.debug("Successfully created TlsPolicy with {}", creator.getClass().getName());
                return tlsPolicy;
            }
            catch(TlsPolicyDescriptorInvalidException e) { throw e; }
            catch(IllegalArgumentException e) {
                throw new TlsPolicyDescriptorInvalidException(e, tlsPolicyDescriptor);
            }
        }
        
        throw new IllegalArgumentException("Unsupported TLS policy choice");
    }
	
}
