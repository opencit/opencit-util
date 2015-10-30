/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
//import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class X509UtilTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testCreateCaCertificate() throws NoSuchAlgorithmException, CertificateParsingException, IOException, CertificateEncodingException, CertificateException {
        KeyPair caKeys = RsaUtil.generateRsaKeyPair(1024);
        X509Certificate caCert = X509Builder.factory().selfSigned("CN=testca", caKeys).expires(30, TimeUnit.DAYS).keyUsageCertificateAuthority().build();
        log.debug("cacert base64: {}", Base64.encodeBase64String(caCert.getEncoded()));
        log.debug("created CA cert with CA flag: {}",caCert.getBasicConstraints());
//        log.debug("HERE IS THE PEM: "+X509Util.encodePemCertificate(caCert));
        log.debug("basic constraints: {}",caCert.getBasicConstraints());
        boolean keyUsage[] = caCert.getKeyUsage();
        if( keyUsage != null ) {
            log.debug("key usage CA: {}",keyUsage[5]);
        }
        else { log.debug("keyUsage boolean[] is NULL"); }
        
        
    // not going to have extended key usages. the keys would be anyExtendedKeyUsage, serverAuth, clientAuth, codeSigning, emailProtection, ipsecEndSystem, ipsecTunnel, ipsecUser, timeStamping, OCSPSigning
            List<String> extKeyUsage = caCert.getExtendedKeyUsage();
            if( extKeyUsage != null ) {
            log.debug("ExtendedKeyUsage count: {}",extKeyUsage.size());
            for(String str: extKeyUsage) { log.debug("ExtendedKeyUsage: {}",str); }
            } else { log.debug("ExtendedKeyUsage list is NULL"); }

    }
    
    
    @Test
    public void testReadCaCertificate() throws CertificateException {
        String pem = //"-----BEGIN CERTIFICATE-----\n" +
"MIIBwjCCASugAwIBAgIIUV/sIM8fxt0wDQYJKoZIhvcNAQELBQAwETEPMA0GA1UEAxMGdGVzdGNh\n" +
"MB4XDTEzMDEwNjA4MTkyNloXDTEzMDIwNTA4MTkyNlowETEPMA0GA1UEAxMGdGVzdGNhMIGfMA0G\n" +
"CSqGSIb3DQEBAQUAA4GNADCBiQKBgQCw4KY7iTN1O4fxVsZFnzqPM3EMDfduTbeNdvj0wIRCMffA\n" +
"lSlb8Ah6HEcw60jB93Fhc+a9ycd9k0VdmcRCAXlmAWd5RSk/Rw1G0Pr4M0rD4keUGGcu9ftXbPnm\n" +
"LM/wjnNaMjCJsItq6n591R7OFxeoWNd+wP5mKQQ0duIyZtMVjQIDAQABoyMwITAOBgNVHQ8BAf8E\n" +
"BAMCAgQwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOBgQCupt8BZuk1yBpuK+eA0SMh\n" +
"EeUXVCL3Hbc5dTjvnVqyUE+G1VFFZVttKuTtk5e2W8kjHY4A6Pab7HuWwlxAVDwBH/1OY3Nij1oS\n" +
"YEjPL8kq/nVEimCV87f2+OEkOH/jIiZPitwGKW+N+rARuBds9GF9s8njz/u5GETCdyuzvAVFQQ==\n" ;
//"-----END CERTIFICATE-----";
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        byte[] der = Base64.decodeBase64(pem);
        ByteArrayInputStream in = new ByteArrayInputStream(der);
        X509Certificate cert = (X509Certificate)certFactory.generateCertificate(in);
        int basicConstraints = cert.getBasicConstraints();
        boolean[] keyUsage = cert.getKeyUsage(); // index 5 is CA
//        List<String> extendedKeyUsage = cert.getExtendedKeyUsage();
        log.debug("CA basic constraint: {}",basicConstraints);
        log.debug("CA key usage: {}",(keyUsage==null?"NULL":keyUsage[5]));
//        log.debug("CA extended key usage: "+extendedKeyUsage==null?"NULL":extendedKeyUsage.get(0));
    }
    
    /**
     * You get this exception:
     *  // InvalidKeyException: IOException: DerInputStream.getLength(): lengthTag=111, too big.
     * If you try to decode the RSA PUBLIC KEY with the -----BEGIN PUBLIC KEY----- and -----END PUBLIC KEY----- tags still in there.
     * You have to strip those out first, then base64-decode the contents, and pass that to the key factory.
     * 
     * @throws Exception 
     */
    @Test
    public void testReadAikKey() throws Exception {
        /*
        String pem = ""+ //-----BEGIN PUBLIC KEY-----\n"+
"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvNEz3+TStAAndHTc1qwT\n"+
"NGvZYyB7DD1FshQf+mbQUGJ9HccOXNn5oHB7fWQjODjlDrYyCs7FclSMTLxA3lHX\n"+
"98QWeWHL2O8t8qrJQQEUWZITmr/ddiNJOOvMeYF0K5if4m84vjgx/pTwwAVyU0Yo\n"+
"MMXPnRozO8o7zSyRsH4jixALDugrsveEjLQI/cIEFvNjqlhyfumHyJKywNkMH1oJ\n"+
"4e/f89FkpeDV694lsLs1jguuLLnvroXYJ5Uzeos+F0Pj1zFDUvhWrjVwxsUfAxS8\n"+
"5uFGTUm6EEl9XiKwi+mgg8ODrY5dh3uE2yKB2T1Qj8BfK55zB8cYbORSsm6/f6Bi\n"+
"BwIDAQAB\n";
//"-----END PUBLIC KEY-----\n";
        */
        String pem = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvNEz3+TStAAndHTc1qwTNGvZYyB7DD1FshQf+mbQUGJ9HccOXNn5oHB7fWQjODjlDrYyCs7FclSMTLxA3lHX98QWeWHL2O8t8qrJQQEUWZITmr/ddiNJOOvMeYF0K5if4m84vjgx/pTwwAVyU0YoMMXPnRozO8o7zSyRsH4jixALDugrsveEjLQI/cIEFvNjqlhyfumHyJKywNkMH1oJ4e/f89FkpeDV694lsLs1jguuLLnvroXYJ5Uzeos+F0Pj1zFDUvhWrjVwxsUfAxS85uFGTUm6EEl9XiKwi+mgg8ODrY5dh3uE2yKB2T1Qj8BfK55zB8cYbORSsm6/f6BiBwIDAQAB";
        byte[] pemBytes = Base64.decodeBase64(pem);
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pemBytes));
//        X509Certificate willNotWork = X509Util.decodePemCertificate(pem);  // you would get an exception like DerInputStream.getLength(): lengthTag=127, too big   ... ebcause this isn't an X509 certificate, it's an RSA public key w/o the certificate.
    }
    
    @Test(expected=InvalidKeySpecException.class)
    public void testReadAikKey2() throws Exception {
        String pem = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvNEz3+TStAAndHTc1qwTNGvZYyB7DD1FshQf+mbQUGJ9HccOXNn5oHB7fWQjODjlDrYyCs7FclSMTLxA3lHX98QWeWHL2O8t8qrJQQEUWZITmr/ddiNJOOvMeYF0K5if4m84vjgx/pTwwAVyU0YoMMXPnRozO8o7zSyRsH4jixALDugrsveEjLQI/cIEFvNjqlhyfumHyJKywNkMH1oJ4e/f89FkpeDV694lsLs1jguuLLnvroXYJ5Uzeos+F0Pj1zFDUvhWrjVwxsUfAxS85uFGTUm6EEl9XiKwi+mgg8ODrY5dh3uE2yKB2T1Qj8BfK55zB8cYbORSsm6/f6BiBwIDAQAB-----END PUBLIC KEY-----";
        byte[] der = Base64.decodeBase64(pem);
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der)); // InvalidKeyException: IOException: DerInputStream.getLength(): lengthTag=111, too big.        
    }
    
    @Test
    public void testReadAikKey3() throws Exception {
        String pem = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvNEz3+TStAAndHTc1qwTNGvZYyB7DD1FshQf+mbQUGJ9HccOXNn5oHB7fWQjODjlDrYyCs7FclSMTLxA3lHX98QWeWHL2O8t8qrJQQEUWZITmr/ddiNJOOvMeYF0K5if4m84vjgx/pTwwAVyU0YoMMXPnRozO8o7zSyRsH4jixALDugrsveEjLQI/cIEFvNjqlhyfumHyJKywNkMH1oJ4e/f89FkpeDV694lsLs1jguuLLnvroXYJ5Uzeos+F0Pj1zFDUvhWrjVwxsUfAxS85uFGTUm6EEl9XiKwi+mgg8ODrY5dh3uE2yKB2T1Qj8BfK55zB8cYbORSsm6/f6BiBwIDAQAB-----END PUBLIC KEY-----";
        PublicKey publicKey = RsaUtil.decodePemPublicKey(pem);
    }
    
    
    @Test
    public void testDecodePemCertificates1() throws CertificateException {
        String pem1 = "-----BEGIN CERTIFICATE-----\n" +
"MIIBwjCCASugAwIBAgIIUV/sIM8fxt0wDQYJKoZIhvcNAQELBQAwETEPMA0GA1UEAxMGdGVzdGNh\n" +
"MB4XDTEzMDEwNjA4MTkyNloXDTEzMDIwNTA4MTkyNlowETEPMA0GA1UEAxMGdGVzdGNhMIGfMA0G\n" +
"CSqGSIb3DQEBAQUAA4GNADCBiQKBgQCw4KY7iTN1O4fxVsZFnzqPM3EMDfduTbeNdvj0wIRCMffA\n" +
"lSlb8Ah6HEcw60jB93Fhc+a9ycd9k0VdmcRCAXlmAWd5RSk/Rw1G0Pr4M0rD4keUGGcu9ftXbPnm\n" +
"LM/wjnNaMjCJsItq6n591R7OFxeoWNd+wP5mKQQ0duIyZtMVjQIDAQABoyMwITAOBgNVHQ8BAf8E\n" +
"BAMCAgQwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOBgQCupt8BZuk1yBpuK+eA0SMh\n" +
"EeUXVCL3Hbc5dTjvnVqyUE+G1VFFZVttKuTtk5e2W8kjHY4A6Pab7HuWwlxAVDwBH/1OY3Nij1oS\n" +
"YEjPL8kq/nVEimCV87f2+OEkOH/jIiZPitwGKW+N+rARuBds9GF9s8njz/u5GETCdyuzvAVFQQ==\n" +
"-----END CERTIFICATE-----";
        List<X509Certificate> certs1 = X509Util.decodePemCertificates(pem1);
        for(X509Certificate cert : certs1) { log.debug("cert: {}", cert.getSubjectX500Principal().getName());
        
        }
        
       
        
        }
    @Test
    public void testDecodePemCertificates2() throws CertificateException {
        String pem2 = "-----BEGIN CERTIFICATE-----\n" +
"MIIBwjCCASugAwIBAgIIUV/sIM8fxt0wDQYJKoZIhvcNAQELBQAwETEPMA0GA1UEAxMGdGVzdGNh\n" +
"MB4XDTEzMDEwNjA4MTkyNloXDTEzMDIwNTA4MTkyNlowETEPMA0GA1UEAxMGdGVzdGNhMIGfMA0G\n" +
"CSqGSIb3DQEBAQUAA4GNADCBiQKBgQCw4KY7iTN1O4fxVsZFnzqPM3EMDfduTbeNdvj0wIRCMffA\n" +
"lSlb8Ah6HEcw60jB93Fhc+a9ycd9k0VdmcRCAXlmAWd5RSk/Rw1G0Pr4M0rD4keUGGcu9ftXbPnm\n" +
"LM/wjnNaMjCJsItq6n591R7OFxeoWNd+wP5mKQQ0duIyZtMVjQIDAQABoyMwITAOBgNVHQ8BAf8E\n" +
"BAMCAgQwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOBgQCupt8BZuk1yBpuK+eA0SMh\n" +
"EeUXVCL3Hbc5dTjvnVqyUE+G1VFFZVttKuTtk5e2W8kjHY4A6Pab7HuWwlxAVDwBH/1OY3Nij1oS\n" +
"YEjPL8kq/nVEimCV87f2+OEkOH/jIiZPitwGKW+N+rARuBds9GF9s8njz/u5GETCdyuzvAVFQQ==\n" +
"-----END CERTIFICATE-----\n" +
                "-----BEGIN CERTIFICATE-----\n" +
"MIIBwjCCASugAwIBAgIIUV/sIM8fxt0wDQYJKoZIhvcNAQELBQAwETEPMA0GA1UEAxMGdGVzdGNh\n" +
"MB4XDTEzMDEwNjA4MTkyNloXDTEzMDIwNTA4MTkyNlowETEPMA0GA1UEAxMGdGVzdGNhMIGfMA0G\n" +
"CSqGSIb3DQEBAQUAA4GNADCBiQKBgQCw4KY7iTN1O4fxVsZFnzqPM3EMDfduTbeNdvj0wIRCMffA\n" +
"lSlb8Ah6HEcw60jB93Fhc+a9ycd9k0VdmcRCAXlmAWd5RSk/Rw1G0Pr4M0rD4keUGGcu9ftXbPnm\n" +
"LM/wjnNaMjCJsItq6n591R7OFxeoWNd+wP5mKQQ0duIyZtMVjQIDAQABoyMwITAOBgNVHQ8BAf8E\n" +
"BAMCAgQwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOBgQCupt8BZuk1yBpuK+eA0SMh\n" +
"EeUXVCL3Hbc5dTjvnVqyUE+G1VFFZVttKuTtk5e2W8kjHY4A6Pab7HuWwlxAVDwBH/1OY3Nij1oS\n" +
"YEjPL8kq/nVEimCV87f2+OEkOH/jIiZPitwGKW+N+rARuBds9GF9s8njz/u5GETCdyuzvAVFQQ==\n" +
"-----END CERTIFICATE-----";
        List<X509Certificate> certs2 = X509Util.decodePemCertificates(pem2);
        for(X509Certificate cert : certs2) { log.debug("cert: {}", cert.getSubjectX500Principal().getName());
        
        }
        
       
        
        }
    
    @Test
    public void testDecodePemCertificate3a() throws CertificateException {
        String pem3 = "-----BEGIN CERTIFICATE-----  MIICvTCCAaWgAwIBAgIGAVAAxUs6MA0GCSqGSIb3DQEBBQUAMBsxGTAXBgNVBAMTEG10d2lsc29u  LXBjYS1haWswHhcNMTUwOTI0MTkxMjIxWhcNMjUwOTIzMTkxMjIxWjAAMIIBIjANBgkqhkiG9w0B  AQEFAAOCAQ8AMIIBCgKCAQEAruRDsUf7ukja+xR0T39QdbGilsCw3aufEqSWS6U0K3WnRyBrZtgz  j0z5kuh3SG4emCwXsVzfCAY/VIk/gtFX+ZSlMAUGk7pxaaMrlKHZbn7dMPQbuft5Wx8BMaMPTxaR  sDWw0xrSEX0Aq6mdIPpf2JRoGLwcs3rrN9KbHo+xumdgYgwUTztF7LGon2fjpr3cLhgffMBDNNss  Cr5t9WhYV/n2uWadYyE5+hRJQr1bs9vBxric2LGgvmvhlP+MAhGp7dSkaTiEz5QeueG557vLEsvD  1UP63iK9vXk8Vj5gj5Pd9efG81F2mauubJwqxikK1KMu8EsCuE/v1bL2xTbDNwIDAQABoyIwIDAe  BgNVHREBAf8EFDASgRBISVMgSWRlbnRpdHkgS2V5MA0GCSqGSIb3DQEBBQUAA4IBAQBwmQoqOXdF  J3Qfem/9i/eVF8VMeIV3CdPmoCPnClvWMdkKe6BtoOCguF/QIRD4Ydo9NxiHtHdavnlM4WVCgQ6J  IXmgJ4qDiXx7juZZBF0Qt+p2NnXxWnIH+QW0YT+5RSM24sjRIPwgFo1raHq+YyFFb4Z00uVuH2u2  NRmouDKD43bzX11pBovLuM5re2oMUxmpOFtV2lacCilJ6f4c9vYdji6o+p0Nkhbmv5kMaoAEWxM6  IV82ZtR3YwbXDUw8lkMpEFMmj9wNJLRVsGBRvcJfZpxl663L5Ar4cckvpRGCzSnwM9yFFnZOngKb  Em/CIFF2aKMzwYN9ZMNY5FEtlBJE  -----END CERTIFICATE----- ";
        List<X509Certificate> certs = X509Util.decodePemCertificates(pem3);
        log.debug("got {} certs", certs.size()); // ZERO!
        for(X509Certificate cert : certs) { log.debug("cert: {}", cert.getSubjectX500Principal().getName());
        
        }
    }
    @Test
    public void testDecodePemCertificate3b() throws CertificateException {
        String pem3 = "-----BEGIN CERTIFICATE-----\n" +
"MIICvTCCAaWgAwIBAgIGAVAAxUs6MA0GCSqGSIb3DQEBBQUAMBsxGTAXBgNVBAMTEG10d2lsc29u\n" +
"LXBjYS1haWswHhcNMTUwOTI0MTkxMjIxWhcNMjUwOTIzMTkxMjIxWjAAMIIBIjANBgkqhkiG9w0B\n" +
"AQEFAAOCAQ8AMIIBCgKCAQEAruRDsUf7ukja+xR0T39QdbGilsCw3aufEqSWS6U0K3WnRyBrZtgz\n" +
"j0z5kuh3SG4emCwXsVzfCAY/VIk/gtFX+ZSlMAUGk7pxaaMrlKHZbn7dMPQbuft5Wx8BMaMPTxaR\n" +
"sDWw0xrSEX0Aq6mdIPpf2JRoGLwcs3rrN9KbHo+xumdgYgwUTztF7LGon2fjpr3cLhgffMBDNNss\n" +
"Cr5t9WhYV/n2uWadYyE5+hRJQr1bs9vBxric2LGgvmvhlP+MAhGp7dSkaTiEz5QeueG557vLEsvD\n" +
"1UP63iK9vXk8Vj5gj5Pd9efG81F2mauubJwqxikK1KMu8EsCuE/v1bL2xTbDNwIDAQABoyIwIDAe\n" +
"BgNVHREBAf8EFDASgRBISVMgSWRlbnRpdHkgS2V5MA0GCSqGSIb3DQEBBQUAA4IBAQBwmQoqOXdF\n" +
"J3Qfem/9i/eVF8VMeIV3CdPmoCPnClvWMdkKe6BtoOCguF/QIRD4Ydo9NxiHtHdavnlM4WVCgQ6J\n" +
"IXmgJ4qDiXx7juZZBF0Qt+p2NnXxWnIH+QW0YT+5RSM24sjRIPwgFo1raHq+YyFFb4Z00uVuH2u2\n" +
"NRmouDKD43bzX11pBovLuM5re2oMUxmpOFtV2lacCilJ6f4c9vYdji6o+p0Nkhbmv5kMaoAEWxM6\n" +
"IV82ZtR3YwbXDUw8lkMpEFMmj9wNJLRVsGBRvcJfZpxl663L5Ar4cckvpRGCzSnwM9yFFnZOngKb\n" +
"Em/CIFF2aKMzwYN9ZMNY5FEtlBJE\n" +
"-----END CERTIFICATE-----";
        List<X509Certificate> certs = X509Util.decodePemCertificates(pem3);
        log.debug("got {} certs", certs.size()); // ONE!
        for(X509Certificate cert : certs) { log.debug("cert: {}", cert.getSubjectX500Principal().getName());
        
        }
    }
    
}
