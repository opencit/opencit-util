/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.xml.dsig;

import com.intel.dcsg.cpg.crypto.digest.Digest;
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
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

/**
 *
 * @author jbuhacoff
 */
public class VerifyXmlSignature {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VerifyXmlSignature.class);

    /**
     * 
     * @param xmlpath path to xml document in project test resources
     * @param certpath path to certificate (PEM) in project test resources
     */
    private boolean isValid(String xmlpath, String certpath) throws IOException, CertificateException, ParserConfigurationException, SAXException, MarshalException, XMLSignatureException {
        log.debug("Validating xml: {} against certificate: {}", xmlpath, certpath);
        String xml = IOUtils.toString(getClass().getResourceAsStream(xmlpath), Charset.forName("UTF-8"));
        String pem = IOUtils.toString(getClass().getResourceAsStream(certpath), Charset.forName("UTF-8"));
        X509Certificate certificate = X509Util.decodePemCertificate(pem);
        log.debug("certificate subject: {}", certificate.getSubjectX500Principal().getName());
        log.debug("certificate issuer: {}", certificate.getIssuerX500Principal().getName());
        boolean isValid = XmlDsigVerify.isValid(xml, certificate);
        log.debug("xml signature valid? {}", isValid);
        return isValid;
    }
    
    @Test
    public void testVMQuoteDocument1() throws IOException, CertificateException, ParserConfigurationException, SAXException, MarshalException, XMLSignatureException {
        assertTrue(isValid("/documents/1/vmquote.xml","/documents/1/vmquote_certificate.pem"));
    }


    /**
2015-10-27 08:45:05,305 DEBUG [main] c.i.m.u.x.d.VerifyXmlSignature [VerifyXmlSignature.java:43] certificate subject: CN=mtwilson,OU=Mt Wilson,O=Intel,L=Folsom,ST=CA,C=US
2015-10-27 08:45:05,315 DEBUG [main] c.i.m.u.x.d.VerifyXmlSignature [VerifyXmlSignature.java:44] certificate issuer: CN=mtwilson,OU=Mt Wilson,O=Intel,L=Folsom,ST=CA,C=US
2015-10-27 08:45:05,460 DEBUG [main] c.i.m.u.x.d.XmlDsigVerify [XmlDsigVerify.java:127] Validating the certificate that signed the XML data. CN=mtwilson,OU=Mt Wilson,O=Intel,L=Folsom,ST=CA,C=US
2015-10-27 08:45:05,478 ERROR [main] c.i.m.u.x.d.XmlDsigVerify [XmlDsigVerify.java:61] Signature failed core validation
2015-10-27 08:45:05,479 ERROR [main] c.i.m.u.x.d.XmlDsigVerify [XmlDsigVerify.java:63] signature validation status: false
2015-10-27 08:45:05,481 ERROR [main] c.i.m.u.x.d.XmlDsigVerify [XmlDsigVerify.java:68] ref[0] validity status: false
2015-10-27 08:45:05,481 DEBUG [main] c.i.m.u.x.d.VerifyXmlSignature [VerifyXmlSignature.java:46] xml signature valid? false 
     */
    @Test
    public void testTrustPolicyDocumentWithSigNs() throws IOException, CertificateException, ParserConfigurationException, SAXException, MarshalException, XMLSignatureException {
//        assertTrue(isValid("/documents/2/trustpolicy_sig.xml","/documents/2/certificate.pem"));
    }

    /**
2015-10-27 08:46:08,952 DEBUG [main] c.i.m.u.x.d.VerifyXmlSignature [VerifyXmlSignature.java:63] certificate subject: CN=mtwilson,OU=Mt Wilson,O=Intel,L=Folsom,ST=CA,C=US
2015-10-27 08:46:08,959 DEBUG [main] c.i.m.u.x.d.VerifyXmlSignature [VerifyXmlSignature.java:64] certificate issuer: CN=mtwilson,OU=Mt Wilson,O=Intel,L=Folsom,ST=CA,C=US
2015-10-27 08:46:09,088 DEBUG [main] c.i.m.u.x.d.XmlDsigVerify [XmlDsigVerify.java:127] Validating the certificate that signed the XML data. CN=mtwilson,OU=Mt Wilson,O=Intel,L=Folsom,ST=CA,C=US
2015-10-27 08:46:09,105 ERROR [main] c.i.m.u.x.d.XmlDsigVerify [XmlDsigVerify.java:61] Signature failed core validation
2015-10-27 08:46:09,106 ERROR [main] c.i.m.u.x.d.XmlDsigVerify [XmlDsigVerify.java:63] signature validation status: false
2015-10-27 08:46:09,108 ERROR [main] c.i.m.u.x.d.XmlDsigVerify [XmlDsigVerify.java:68] ref[0] validity status: false
2015-10-27 08:46:09,109 DEBUG [main] c.i.m.u.x.d.VerifyXmlSignature [VerifyXmlSignature.java:66] xml signature valid? false
     */
    @Test
    public void testTrustPolicyDocumentWithSigNoNs() throws IOException, CertificateException, ParserConfigurationException, SAXException, MarshalException, XMLSignatureException {
//        assertTrue(isValid("/documents/2/trustpolicy_sig_no_ns.xml","/documents/2/certificate.pem"));
//        isValid("/documents/2/trustpolicy_sig.xml","/documents/2/certificate.pem");
//        isValid("/documents/2/trustpolicy_sig2.xml","/documents/2/certificate.pem");
//        isValid("/documents/2/trustpolicy_sig3.xml","/documents/2/certificate.pem");
//        isValid("/documents/2/trustpolicy_sig_nospaces.xml","/documents/2/certificate.pem");
//        isValid("/documents/2/trustpolicy_sig_no_ns.xml","/documents/2/certificate.pem");
//        isValid("/documents/2/trustpolicy_sig_no_ns_newline.xml","/documents/2/certificate.pem");
//        isValid("/documents/2/trustpolicy_sig_no_ns_nospaces.xml","/documents/2/certificate.pem");
//        isValid("/documents/3/trustpolicy_signed_3.xml","/documents/3/certificate.pem");
//        isValid("/documents/3/trustpolicy_signed_3_edited.xml","/documents/3/certificate.pem");
//        isValid("/documents/4/trustpolicy_signed_4.xml","/documents/4/certificate.pem");
//        isValid("/documents/3/trustpolicy_signed_3_edited.xml","/documents/3/certificate.pem");
//        isValid("/documents/4/trustpolicy_signed_4_edited.xml","/documents/4/certificate.pem");
        isValid("/documents/5/trustpolicy_1_signed.xml","/documents/5/certificate.pem");
//        isValid("/documents/5/trustpolicy_2_jaxb.xml","/documents/5/certificate.pem");
//        isValid("/documents/5/trustpolicy_3_formatted.xml","/documents/5/certificate.pem");
        isValid("/documents/5/trustpolicy_4_edited.xml","/documents/5/certificate.pem");
        isValid("/documents/5/trustpolicy_5_edited.xml","/documents/5/certificate.pem");
    }
    
    private String sha1(String text) {
        String sha1 = Digest.sha1().digest(text, Charset.forName("UTF-8")).toBase64();
        log.debug("sha1: {}", sha1);
        return sha1;
    }
    
    @Test
    public void generateHash() {
        String text1 = "        <ns3:SignedInfo>\n" +
"            <ns3:CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/>\n" +
"            <ns3:SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/>\n" +
"            <ns3:Reference URI=\"\">\n" +
"                <ns3:Transforms>\n" +
"                    <ns3:Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/>\n" +
"                </ns3:Transforms>\n" +
"                <ns3:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>\n" +
"                <ns3:DigestValue>daB/n0k/7O0YjgKZ4VmHi3oxAys=</ns3:DigestValue>\n" +
"            </ns3:Reference>\n" +
"        </ns3:SignedInfo>";
        sha1(text1);
        String text2 = "<SignedInfo>\n" +
"            <CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/>\n" +
"            <SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/>\n" +
"            <Reference URI=\"\">\n" +
"                <Transforms>\n" +
"                    <Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/>\n" +
"                </Transforms>\n" +
"                <DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>\n" +
"                <DigestValue>daB/n0k/7O0YjgKZ4VmHi3oxAys=</DigestValue>\n" +
"            </Reference>\n" +
"        </SignedInfo>";
        sha1(text2);
        String text3 = "<SignedInfo>\n" +
"<CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/>\n" +
"<SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/>\n" +
"<Reference URI=\"\">\n" +
"<Transforms>\n" +
"<Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/>\n" +
"</Transforms>\n" +
"<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>\n" +
"<DigestValue>daB/n0k/7O0YjgKZ4VmHi3oxAys=</DigestValue>\n" +
"</Reference>\n" +
"</SignedInfo>";
        sha1(text3);
        String text4 = "<SignedInfo>" +
"<CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/>" +
"<SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/>" +
"<Reference URI=\"\">" +
"<Transforms>" +
"<Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/>" +
"</Transforms>" +
"<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>" +
"<DigestValue>daB/n0k/7O0YjgKZ4VmHi3oxAys=</DigestValue>" +
"</Reference>" +
"</SignedInfo>";
        sha1(text4);
    }
}
