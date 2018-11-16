/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.core.data.bundle.contributors;

import com.intel.dcsg.cpg.crypto.digest.Digest;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.io.Resource;
import com.intel.dcsg.cpg.io.pem.Pem;
import com.intel.dcsg.cpg.io.pem.PemLikeParser;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.core.data.bundle.Bundle;
import com.intel.mtwilson.core.data.bundle.Contributor;
import com.intel.mtwilson.core.data.bundle.Namespace;
import com.intel.mtwilson.util.crypto.keystore.PublicKeyX509CertificateStore;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyStoreException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public abstract class AbstractImportCertificatesPemToKeystore implements Contributor {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractImportCertificatesPemToKeystore.class);
    
    abstract protected String getNamespace();
    
    abstract protected String getPath();
    
    abstract protected Resource getKeystoreResource();
    
    abstract protected Password getKeystorePassword();
    
    /**
     * 
     * @param certificate
     * @return sha-256 digest of the certificate; override to choose an alias some other way
     * @throws CertificateEncodingException 
     */
    protected String getAlias(X509Certificate certificate) throws CertificateEncodingException {
        return Digest.sha256().digest(certificate.getEncoded()).toHex();
    }
    
    @Override
    public void receive(Bundle bundle) {
        Namespace namespace = bundle.namespace(getNamespace()); // MTWILSON_CONFIGURATION_NAMESPACE
        String path = getPath();//SAML_FILENAME
        if (namespace.contains(path)) {
            try {
                byte[] pem = namespace.get(path);
                List<Pem> certs = PemLikeParser.parse(new String(pem, Charset.forName("UTF-8")));
                try {
                    Resource keystoreResource = getKeystoreResource(); //setup.getSamlCertificatesKeystoreFile();
                    Password keystorePassword = getKeystorePassword(); // setup.getSamlCertificatesKeystorePassword();
                    try (PublicKeyX509CertificateStore store = new PublicKeyX509CertificateStore("JKS", keystoreResource, keystorePassword.toCharArray())) {
                        for (Pem cert : certs) {
                            try {
                                X509Certificate x509 = X509Util.decodeDerCertificate(cert.getContent());
                                String alias = getAlias(x509);
                                store.set(alias, x509);
                            } catch (CertificateException | KeyStoreException e) {
                                log.warn("Unable to decode certificate from {}", path, e);
                            }
                        }
                    }
                } catch (KeyStoreException e) {
                    log.error("Failed to open configuration", e);
                }
            } catch (IOException e) {
                log.error("Failed to read SAML.pem from data bundle", e);
            }
        }
    }

}
