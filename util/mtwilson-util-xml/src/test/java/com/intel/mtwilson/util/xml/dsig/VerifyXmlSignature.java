/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.xml.dsig;

import com.intel.dcsg.cpg.x509.X509Util;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author jbuhacoff
 */
public class VerifyXmlSignature {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VerifyXmlSignature.class);

    @Test
    public void testVMQuoteDocument1() throws IOException, CertificateException, ParserConfigurationException, SAXException, MarshalException, XMLSignatureException {
        String xml = IOUtils.toString(getClass().getResourceAsStream("/documents/1/vmquote.xml"), Charset.forName("UTF-8"));
        String pem = IOUtils.toString(getClass().getResourceAsStream("/documents/1/vmquote_certificate.pem"), Charset.forName("UTF-8"));
        X509Certificate certificate = X509Util.decodePemCertificate(pem);
        log.debug("certificate subject: {}", certificate.getSubjectX500Principal().getName());
        log.debug("certificate issuer: {}", certificate.getIssuerX500Principal().getName());
        boolean isValid = XmlDsigVerify.isValid(xml, certificate);
        log.debug("xml signature valid? {}", isValid);
    }
}
