/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.client;

import java.util.Properties;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.mtwilson.util.crypto.keystore.PasswordKeyStore;
import java.net.URL;
import java.security.KeyStoreException;

/**
 *
 * @author jbuhacoff
 */
public class MtWilsonClient extends JaxrsClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MtWilsonClient.class);
    private Configuration configuration = null;
    private PasswordKeyStore passwordKeyStore = null;
    
    public MtWilsonClient(URL url) throws Exception {
        super(JaxrsClientBuilder.factory().url(url).build());
    }

    public MtWilsonClient(Properties properties) throws Exception {
        super(JaxrsClientBuilder.factory().configuration(properties).build());
        this.configuration = new PropertiesConfiguration(properties);
    }
    public MtWilsonClient(Configuration configuration) throws Exception {
        super(JaxrsClientBuilder.factory().configuration(configuration).build());
        this.configuration = configuration;
    }
    
    public MtWilsonClient(Properties properties, TlsConnection tlsConnection) throws Exception {
        super(JaxrsClientBuilder.factory().configuration(properties).tlsConnection(tlsConnection).build());
        this.configuration = new PropertiesConfiguration(properties);
    }

    public MtWilsonClient(Configuration configuration, PasswordKeyStore passwordKeyStore) throws Exception {
        super(JaxrsClientBuilder.factory().configuration(configuration).passwords(passwordKeyStore).build());
        this.configuration = configuration;
        this.passwordKeyStore = passwordKeyStore;
    }
    
    public MtWilsonClient(Properties properties, TlsConnection tlsConnection, PasswordKeyStore passwordKeyStore) throws Exception {
        super(JaxrsClientBuilder.factory().configuration(properties).passwords(passwordKeyStore).tlsConnection(tlsConnection).build());
        this.configuration = new PropertiesConfiguration(properties);
        this.passwordKeyStore = passwordKeyStore;
    }

    public Configuration getConfiguration() { return configuration; }
    
    protected Password getPassword(String... aliases) {
        // first check the password key store
        if( passwordKeyStore != null ) {
            for (String alias : aliases) {
                try {
                    if (passwordKeyStore.contains(alias)) {
                        return passwordKeyStore.get(alias);
                    }
                } catch (KeyStoreException e) {
                    log.error("Keystore failed to retrieve password: {}", alias, e);
                }
            }
        }
        // second check the configuration (compatibility with existing code)
        if( configuration != null ) {
            for (String alias : aliases) {
                String property = configuration.get(alias);
                if( property != null ) {
                    return new Password(property);
                }
            }
        }
        // password was not found in keystore or configuration
        return null;
    }
    
}
