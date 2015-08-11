/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.setup;

import com.intel.dcsg.cpg.crypto.Md5Digest;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.iso8601.Iso8601Date;
import com.intel.dcsg.cpg.net.NetUtils;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.configuration.PasswordVaultFactory;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.util.crypto.keystore.PasswordKeyStore;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Creates a TLS keypair and self-signed certificate.
 * 
 * Very similar to Trust Agent's CreateTlsKeypair SetupTask
 * 
 * @author jbuhacoff
 */
public class JettyTlsKeystore extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JettyTlsKeystore.class);
    
    // constants
    private static final String TLS_ALIAS = "jetty";
    
    // configuration keys
    private static final String KMS_TLS_CERT_DN = "jetty.tls.cert.dn";
    private static final String KMS_TLS_CERT_IP = "jetty.tls.cert.ip";
    private static final String KMS_TLS_CERT_DNS = "jetty.tls.cert.dns";
    public static final String JAVAX_NET_SSL_KEYSTORE = "javax.net.ssl.keyStore";
    public static final String JAVAX_NET_SSL_KEYSTOREPASSWORD = "javax.net.ssl.keyStorePassword";
    public static final String ENDPOINT_URL = "endpoint.url";
    
    private File keystoreFile;
    private File propertiesFile;
    private Password keystorePassword;
    private String dn;
    private String[] ip;
    private String[] dns;
    
    @Override
    protected void configure() throws Exception {
        
        String keystorePath = getConfiguration().get(JAVAX_NET_SSL_KEYSTORE, null);
        if( keystorePath == null ) {
            keystorePath = Folders.configuration()+File.separator+"keystore.jks";
        }
        keystoreFile = new File(keystorePath);
        
        // to avoid putting any passwords in the configuration file, we
        // get the password from the password vault
        try(PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(getConfiguration())) {
            if( passwordVault.contains(JAVAX_NET_SSL_KEYSTOREPASSWORD)) {
                keystorePassword = passwordVault.get(JAVAX_NET_SSL_KEYSTOREPASSWORD);
            }
        }
        
        /**
         * NOTE: this is NOT the encrypted configuration file, it's a plaintext
         * Java Properties file to store the TLS certificate fingerprints so
         * the administrator can verify a TLS connection to the KMS when using
         * self-signed certificates
         */
        propertiesFile = new File(Folders.configuration()+File.separator+"https.properties"); 
        
        // if we already have a keystore file, then we need to know the existing keystore password
        // otherwise it's ok for password to be missing (new install, or creating new keystore) and
        // we'll generate one in execute()
        if( keystoreFile.exists() ) {
            if( keystorePassword == null || keystorePassword.toCharArray().length == 0 ) { configuration("Keystore password has not been generated"); }
        }
        
        dn = getConfiguration().get(KMS_TLS_CERT_DN, "CN=kms"); //trustagentConfiguration.getTrustagentTlsCertDn();
        // we need to know our own local ip addresses/hostname in order to add them to the ssl cert
        ip = getTrustagentTlsCertIpArray();
        dns = getTrustagentTlsCertDnsArray();
        if( dn == null || dn.isEmpty() ) { configuration("DN not configured"); }
        // NOTE: keystore file itself does not need to be checked, we will create it automatically in execute() if it does not exist
        if( (ip == null ? 0 : ip.length) + (dns == null ? 0 : dns.length) == 0 ) {
            configuration("At least one IP or DNS alternative name must be configured");
        }
    }

    @Override
    protected void validate() throws Exception {
        if( !keystoreFile.exists() ) {
            validation("Keystore file was not created");
            return;
        }
        if( keystorePassword == null || keystorePassword.toCharArray().length == 0) {
            validation("Keystore password has not been generated");
            return;
        }
        SimpleKeystore keystore = new SimpleKeystore(new FileResource(keystoreFile), keystorePassword);
        RsaCredentialX509 credential;
        try {
            credential = keystore.getRsaCredentialX509(TLS_ALIAS, keystorePassword);
            log.debug("Found TLS key {}", credential.getCertificate().getSubjectX500Principal().getName());
        } catch (FileNotFoundException e) {
            log.warn("Keystore does not contain the specified key [{}]", TLS_ALIAS);
            validation("Keystore does not contain the specified key %s", TLS_ALIAS);
        }
        catch(java.security.UnrecoverableKeyException e) {
            log.debug("Incorrect password for existing key; will create new key: {}", e.getMessage());
            validation("Key must be recreated");
        }
        catch(NullPointerException e) {
            log.debug("Invalid TLS certificate");
            validation("Certificate must be recreated");
        }
    }

    @Override
    protected void execute() throws Exception {
        // create the keypair
        KeyPair keypair = RsaUtil.generateRsaKeyPair(2048);
        X509Builder builder = X509Builder.factory()
                .selfSigned(dn, keypair)
                .expires(3650, TimeUnit.DAYS) 
                .keyUsageKeyEncipherment();
        // NOTE:  right now we are creating a self-signed cert but if we have
        //        the mtwilson api url, username, and password, we could submit
        //        a certificate signing request there and have our cert signed
        //        by mtwilson's ca, and then the ssl policy for this host in 
        //        mtwilson could be "signed by trusted ca" instead of
        //        "that specific cert"
        if( ip != null ) {
            for(String san : ip) {
                log.debug("Adding Subject Alternative Name (SAN) with IP address: {}", san);
                builder.ipAlternativeName(san.trim());
            }
        }
        if( dns != null ) {
            for(String san : dns) {
                log.debug("Adding Subject Alternative Name (SAN) with Domain Name: {}", san);
                builder.dnsAlternativeName(san.trim());
            }
        }
        X509Certificate tlscert = builder.build();
        
        /**
         * Log the same information to a plain text file so admin can easily
         * copy it as necessary for use in a TLS policy or to verify the 
         * server's self-signed TLS certificate in the browser.
         * 
         * NOTE: this is NOT the encrypted configuration file, it's a plaintext
         * Java Properties file to store the TLS certificate fingerprints so
         * the administrator can verify a TLS connection to the KMS when using
         * self-signed certificates
         */
        Properties properties = new Properties();
        if( propertiesFile.exists() ) {
            properties.load(new StringReader(FileUtils.readFileToString(propertiesFile, Charset.forName("UTF-8"))));
        }
        properties.setProperty("kms.tls.cert.md5", Md5Digest.digestOf(tlscert.getEncoded()).toString());
        properties.setProperty("kms.tls.cert.sha1", Sha1Digest.digestOf(tlscert.getEncoded()).toString());
        properties.setProperty("kms.tls.cert.sha256", Sha256Digest.digestOf(tlscert.getEncoded()).toString());
        StringWriter writer = new StringWriter();
        properties.store(writer, String.format("updated on %s", Iso8601Date.format(new Date())));
        FileUtils.write(propertiesFile, writer.toString(), Charset.forName("UTF-8"));
        log.debug("Wrote https.properties: {}", writer.toString().replaceAll("[\\r\\n]", "|"));
        
        // make sure we have a keystore password, generate if necessary
        if( keystorePassword == null || keystorePassword.toCharArray().length == 0 ) {
            keystorePassword = new Password(RandomUtil.randomBase64String(8).replace("=","_").toCharArray());
            log.info("Generated random keystore password"); 
        }
        
        // look for an existing tls keypair and delete it
        SimpleKeystore keystore = new SimpleKeystore(new FileResource(keystoreFile), keystorePassword);
        try {
//            String alias = String.format("%s (ssl)", TLS_ALIAS);
            String alias = TLS_ALIAS;
            List<String> aliases = Arrays.asList(keystore.aliases());
            if( aliases.contains(alias) ) {
                keystore.delete(alias);
            }
        }
        catch(KeyStoreException | KeyManagementException e) {
            log.debug("Cannot remove existing tls keypair", e);
        }
        // store it in the keystore
        keystore.addKeyPairX509(keypair.getPrivate(), tlscert, TLS_ALIAS, keystorePassword);
        keystore.save();
        
        // save the settings in configuration
        getConfiguration().set(JAVAX_NET_SSL_KEYSTORE, keystoreFile.getAbsolutePath());
        getConfiguration().set(KMS_TLS_CERT_DN, dn);
        if( ip != null ) {
            getConfiguration().set(KMS_TLS_CERT_IP, StringUtils.join(ip, ","));
        }
        if( dns != null ) {
            getConfiguration().set(KMS_TLS_CERT_DNS, StringUtils.join(dns, ","));
        }
        
        // save the password to the password vault
        try(PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(getConfiguration())) {
            passwordVault.set(JAVAX_NET_SSL_KEYSTOREPASSWORD, keystorePassword);
        }
        
        // save a special endpoint url parameter used to generate the transfer key links
        getConfiguration().set(ENDPOINT_URL, getEndpoint());
    }
    

    private String getEndpoint() {
        // do we have a DNS name configured?
        String endpointHost = null;
        if( dns != null ) {
            for(String hostname : dns) {
                if( !hostname.equals("localhost") ) {
                    endpointHost = hostname;
                }
            }
        }
        // if no DNS name, do we have an external IP address configured?
        if( endpointHost == null && ip != null ) {
            for(String hostname : ip) {
                if( !hostname.equals("127.0.0.1")) {
                    endpointHost = hostname;
                }
            }
        }
        // if no DNS or external IP, just use "localhost" as default
        if( endpointHost == null ) {
            endpointHost = "localhost";
        }
        // do we have a custom port or default port?
        String port = getConfiguration().get("jetty.port", "80");
        if( port.equals("80") ) {
            return String.format("http://%s", endpointHost); //  http://localhost
        }
        else {
            return String.format("http://%s:%s", endpointHost, port);  //  http://localhost:80
        }
    }    
    
    // note: duplicated from TrustagentConfiguration
    public String getTrustagentTlsCertIp() {
        return getConfiguration().get(KMS_TLS_CERT_IP, "");
    }
    // note: duplicated from TrustagentConfiguration
    public String[] getTrustagentTlsCertIpArray() throws SocketException {
//        return getConfiguration().getString(KMS_TLS_CERT_IP, "127.0.0.1").split(",");
        String[] TlsCertIPs = getConfiguration().get(KMS_TLS_CERT_IP, "").split(",");
        if (TlsCertIPs != null && !TlsCertIPs[0].isEmpty()) {
            log.debug("Retrieved IPs from configuration: {}", (Object[])TlsCertIPs);
            return TlsCertIPs;
        }
        List<String> TlsCertIPsList = NetUtils.getNetworkAddressList(); // never returns null but may be empty
        String[] ipListArray = new String[TlsCertIPsList.size()];
        if (ipListArray.length > 0) {
            log.debug("Retrieved IPs from network configuration: {}", (Object[])ipListArray);
            return TlsCertIPsList.toArray(ipListArray);
        }
        log.debug("Returning default IP address [127.0.0.1]");
        return new String[]{"127.0.0.1"};
    }
    // note: duplicated from TrustagentConfiguration
    public String getTrustagentTlsCertDns() {
        return getConfiguration().get(KMS_TLS_CERT_DNS, "");
    }
    // note: duplicated from TrustagentConfiguration
    public String[] getTrustagentTlsCertDnsArray() throws SocketException {
//        return getConfiguration().getString(KMS_TLS_CERT_DNS, "localhost").split(",");
        String[] TlsCertDNs = getConfiguration().get(KMS_TLS_CERT_DNS, "").split(",");
        if (TlsCertDNs != null && !TlsCertDNs[0].isEmpty()) {
            log.debug("Retrieved Domain Names from configuration: {}", (Object[])TlsCertDNs);
            return TlsCertDNs;
        }
        List<String> TlsCertDNsList = NetUtils.getNetworkHostnameList(); // never returns null but may be empty
        String[] dnListArray = new String[TlsCertDNsList.size()];
        if (dnListArray.length > 0) {
            log.debug("Retrieved Domain Names from network configuration: {}", (Object[])dnListArray);
            return TlsCertDNsList.toArray(dnListArray);
        }
        log.debug("Returning default Domain Name [localhost]");
        return new String[]{"localhost"};
    }
    
}
