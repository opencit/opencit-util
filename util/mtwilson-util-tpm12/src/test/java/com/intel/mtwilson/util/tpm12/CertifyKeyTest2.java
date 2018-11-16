/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.tpm12;

import com.intel.dcsg.cpg.x509.X509Util;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class CertifyKeyTest2 {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertifyKeyTest2.class);

    private static X509Certificate aik;
    private static X509Certificate aikIssuer;
    private static X509Certificate bindingKey;
    private static X509Certificate bindingKeyIssuer;
    private static byte[] bindingKeySignature;
    
    @BeforeClass
    public static void loadResources() throws IOException, CertificateException {
        aik = X509Util.decodeDerCertificate(IOUtils.toByteArray(CertifyKeyTest2.class.getResourceAsStream("/test2/aik.pem")));
        aikIssuer = X509Util.decodeDerCertificate(IOUtils.toByteArray(CertifyKeyTest2.class.getResourceAsStream("/test2/aik.issuer.pem")));
        bindingKey = X509Util.decodeDerCertificate(IOUtils.toByteArray(CertifyKeyTest2.class.getResourceAsStream("/test2/bindingkey.pem")));
        bindingKeyIssuer = X509Util.decodeDerCertificate(IOUtils.toByteArray(CertifyKeyTest2.class.getResourceAsStream("/test2/bindingkey.issuer.pem")));
        bindingKeySignature = Base64.decodeBase64(IOUtils.toByteArray(CertifyKeyTest2.class.getResourceAsStream("/test2/bindingkey.sig.base64")));
       log.debug("Loaded binding key signature, {} bytes", bindingKeySignature.length);
    }
    
    @Test
    public void testVerifyBindingKeyCertificate() {
       byte[] signature = bindingKey.getExtensionValue(CertifyKey.TCG_STRUCTURE_CERTIFY_INFO_SIGNATURE_OID);
       log.debug("Signature extension in certificate is {} byte", signature.length);
       boolean verified = CertifyKey.verifyTpmBindingKeyCertificate(bindingKey, aik.getPublicKey());
       log.debug("Verified? {}", verified);
    }
    
    @Test
    public void testBase64Encode() {
        byte[] data = new byte[256];
        for(int i=0; i<256; i++) { data[i] = (byte)i; }
        log.debug("base64 without breaklines: {}", TpmUtils.base64encode(data, false));
        log.debug("base64 with breaklines: {}", TpmUtils.base64encode(data, true));
    }
}
