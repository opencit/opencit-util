/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.jetty9;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.kms.setup.JettyPorts;
import com.intel.kms.setup.JettyTlsKeystore;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.core.PasswordVaultFactory;
import com.intel.mtwilson.util.crypto.keystore.PasswordKeyStore;
import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * How to run the server:
 *
 * <pre>
 * kms start
 * </pre>
 *
 * Old way:
 * <pre>
 * java -jar kms-1.0-SNAPSHOT-with-dependencies.jar start
 * </pre>
 *
 * How to access the server:
 *
 * http://localhost:80/
 *
 *
 * javax.net.ssl.keyStore (no default; must be provided)
 * javax.net.ssl.keyStorePassword (no default, must be provided) jetty.port
 * (default 80) jetty.secure.port (default 443)
 *
 * When using the launcher, export KMS_PASSWORD=password to read the
 * javax.net.ssl.keyStore and .keyStorePassword properties from the encrypted
 * configuration.
 *
 * When running independently, you can set those properties on the java command
 * line with -Djavax.net.ssl.keyStore=keystore.jks and
 * -Djavax.net.ssl.keyStorePassword=password.
 *
 * @author jbuhacoff
 */
public class StartHttpServer implements Runnable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StartHttpServer.class);
    // configuration keys
    public final static String JETTY_HYPERTEXT = "jetty.hypertext";
    public final static String JETTY_WEBXML = "jetty.webxml";
    public final static String JETTY_THREAD_MIN = "jetty.thread.min";
    public final static String JETTY_THREAD_MAX = "jetty.thread.max";
    public final static int JETTY_THREAD_MAX_MULTIPLIER = 16;
    public static Server jetty;
    private Configuration configuration;
    
    private int computeMinThreads() {
        return Runtime.getRuntime().availableProcessors() + 1;
    }
    
    private int computeMaxThreads() {
        return Runtime.getRuntime().availableProcessors() * JETTY_THREAD_MAX_MULTIPLIER;
    }
    
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    protected File getKeystoreFile() {
        return new File(configuration.get(JettyTlsKeystore.JAVAX_NET_SSL_KEYSTORE, Folders.configuration() + File.separator + "keystore.jks"));
    }

    protected Password getKeystorePassword() throws KeyStoreException, IOException {
        try (PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(configuration)) {
            if (passwordVault.contains(JettyTlsKeystore.JAVAX_NET_SSL_KEYSTOREPASSWORD)) {
                return passwordVault.get(JettyTlsKeystore.JAVAX_NET_SSL_KEYSTOREPASSWORD);
            }
            return null;
        }
    }

    public Integer getHttpPort() {
        return Integer.valueOf(configuration.get(JettyPorts.JETTY_PORT, "80"));
    }

    public Integer getHttpsPort() {
        return Integer.valueOf(configuration.get(JettyPorts.JETTY_SECURE_PORT, "443"));
    }

    @Override
    public void run() {
        try {
            configuration = ConfigurationFactory.getConfiguration();
            int minThreads = Integer.parseInt(configuration.get(JETTY_THREAD_MIN, "0"));
            int maxThreads = Integer.parseInt(configuration.get(JETTY_THREAD_MAX, "0"));
            
            if (minThreads < 1) {
                minThreads = computeMinThreads();
            }
            if (maxThreads < 1) {
                maxThreads = Math.max(computeMaxThreads(), 300);
            }
            
            if (minThreads > maxThreads) {
                minThreads = Math.max(1, maxThreads - 1);
            }
            
            QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads);
            jetty = new Server(threadPool);
            
            startJettyHttpServer();
        } catch (IOException e) {
            log.debug("cannot start jetty http server");
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts forward slashes to the platform path separator (forward on
     * linux/mac, backslash on windows)
     *
     * Example:
     * <pre>
     * path("C:/kms/configuration") on Windows would produce "C:\\kms\\configuration"
     * </pre>
     *
     * @param pathForwardSlashes
     * @return
     */
    /*
     private String path(String pathForwardSlashes) {
     return pathForwardSlashes.replace("/", File.separator);
     }
     */
    /**
     * URLs with a file or jar:file scheme require forward slashes on both Linux
     * and Windows. So given a platform absolute path which could have either
     * forward slashes (Linux) or back slashes (Windows) this will convert those
     * slashes to forward slashes.
     *
     * Example:
     * <pre>
     * "jar:file://" + jarpath(absolutePathToJar) + "!/path/within/jar"
     * </pre>
     *
     * @param pathPlatformSlashes
     * @return
     */
    /*
    private String jarpath(String pathPlatformSlashes) {
        return pathPlatformSlashes.replace(File.separator, "/");
    }
    */

    /**
     * PROTOTYPE IMPLEMENTATION: Assumes an "html5" feature containing static
     * resources in the file system. The static resources provided by kms-html5
     * are available under the URL path /v1/resources. So the minimum HTML
     * content needed to make things work out of the box without the user
     * knowing the full URL is to put an index.html file in
     * KMS_HOME/features/html5/index.html with a splash screen and redirect to
     * "/v1/resources/index.html"
     *
     * CIT 3.0 IMPLMENTATION: Assumes a "mtwilson-core-html5" feature containing
     * static resources in the file system. The static resources provided by
     * mtwilson-core-html5 include a splash screen and automatic discovery of
     * installed features that need to integrate with the user interface. The
     * entry point to the application is
     * KMS_HOME/features/mtwilson-core-html5/index.html
     *
     * @return location of hypertext directory either as relative path, absolute
     * file path, or jar resource path
     */
    public String getHypertextUrl() {
        return configuration.get(JETTY_HYPERTEXT, Folders.features("mtwilson-core-html5") + File.separator + "html5");
    }

    /**
     * Default is used during development so you can run "kms start" from the
     * dcg_security-kms source directory.
     *
     * @return location of web.xml either as relative path, absolute file path,
     * or absolute URL
     */
    public String getDescriptorUrl() {
        return configuration.get(JETTY_WEBXML, Folders.features("servlet3") + File.separator + "WEB-INF" + File.separator + "web.xml");
    }

    /*
     * Environment variables:
     * JETTY_HYPERTEXT should be relative or absolute path to the hypertext folder containing index.html and related resources
     * JETTY_WEBXML should be relative or absolute path to the web.xml file 
     * JETTY_HTTP_PORT should be the port number on which jetty should listen for insecure connections
     * JETTY_HTTPS_PORT should be the port number on which jetty should listen for secure connections
     * 
     * Defaults are relative paths from the project's source directory, for convenience during development.
     * Default ports are 80,443
     * 
     * Example of Windows relative paths to files in a JAR:
     * set JETTY_HYPERTEXT=jar:file:./kms-jetty9-0.1-SNAPSHOT.jar!/hypertext
     * set JETTY_WEBXML=jar:file:./kms-jetty9-0.1-SNAPSHOT.jar!/WEB-INF/web.xml
     * 
     * Example of Windows absolute path to files in a JAR:
     * set JETTY_HYPERTEXT=jar:file:///C:/Users/username/kms/kms-jetty9-0.1-SNAPSHOT.jar!/hypertext
     * set JETTY_WEBXML=jar:file:///C:/Users/username/kms/kms-jetty9-0.1-SNAPSHOT.jar!/WEB-INF/web.xml
     * 
     * Example of Linux relative paths to files in a JAR:
     * export JETTY_HYPERTEXT=jar:file:./kms-jetty9-0.1-SNAPSHOT.jar!/hypertext
     * export JETTY_WEBXML=jar:file:./kms-jetty9-0.1-SNAPSHOT.jar!/WEB-INF/web.xml
     * 
     * Example of Linux absolute paths to files in a JAR:
     * export JETTY_HYPERTEXT=jar:file:///opt/kms/java/kms-jetty9-0.1-SNAPSHOT.jar!/hypertext
     * export JETTY_WEBXML=jar:file:///opt/kms/java/kms-jetty9-0.1-SNAPSHOT.jar!/WEB-INF/web.xml
     * 
     * See also: http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty
     */
    public void startJettyHttpServer() {
        // default configuration allows running "java -jar artifact-with-dependencies.jar" from maven project directory
        // in production, set the variables to point to the installed resources
        String resourceBase = getHypertextUrl();
        String descriptor = getDescriptorUrl();

        log.debug("{}={}", JETTY_HYPERTEXT, resourceBase);
        log.debug("{}={}", JETTY_WEBXML, descriptor);

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setResourceBase(resourceBase);
        webapp.setDescriptor(descriptor); // can also be relative to jar like WEB-INF\\web.xml
        webapp.setDefaultsDescriptor(null); // turn off jsp support
        webapp.setParentLoaderPriority(true);

        // common configuration for http/https
        HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setSecureScheme("https");
        httpConfiguration.setSecurePort(getHttpsPort());
        httpConfiguration.setOutputBufferSize(32768);
        httpConfiguration.setRequestHeaderSize(8192);
        httpConfiguration.setResponseHeaderSize(8192);

        // https-specific configuration
        HttpConfiguration httpsConfiguration = new HttpConfiguration(httpConfiguration);
        httpsConfiguration.addCustomizer(new SecureRequestCustomizer()); // adds ssl session id's and certificate information to request attributes

        // http connector
        ServerConnector http = new ServerConnector(jetty, new ConnectionFactory[]{new HttpConnectionFactory(httpConfiguration)});
        http.setPort(getHttpPort());
        log.debug("{}={}", JettyPorts.JETTY_PORT, http.getPort());

        // https connector
        try {
            SslContextFactory sslConnectionFactory = new SslContextFactory();
            sslConnectionFactory.setKeyStorePath(getKeystoreFile().getAbsolutePath());
            Password keystorePassword = getKeystorePassword();
            if (keystorePassword != null) {
                sslConnectionFactory.setKeyStorePassword(new String(keystorePassword.toCharArray()));
            }
//            sslConnectionFactory.setIncludeProtocols("TLSv1", "TLSv1.1", "TLSv1.2");
            sslConnectionFactory.setExcludeProtocols("SSL", "SSLv2", "SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1");
            sslConnectionFactory.setIncludeCipherSuites(
                    "TLS_DHE_RSA.*", "TLS_ECDHE.*"
            );
            sslConnectionFactory.setExcludeCipherSuites(
                    ".*NULL.*", ".*RC4.*", ".*MD5.*", ".*DES.*", ".*DSS.*",
                    "TLS_DHE_RSA_WITH_AES_128_CBC_SHA"
            );
            sslConnectionFactory.setRenegotiationAllowed(false);
            ServerConnector https = new ServerConnector(jetty, new ConnectionFactory[]{new SslConnectionFactory(sslConnectionFactory, "http/1.1"), new HttpConnectionFactory(httpsConfiguration)});
            https.setPort(getHttpsPort());
            log.debug("{}={}", JettyPorts.JETTY_SECURE_PORT, https.getPort());

            jetty.setConnectors(new Connector[]{http, https});
            jetty.setHandler(webapp);

            jetty.start();
            log.info("Started HTTP service: {}", jetty.getURI().toURL().toExternalForm());
        } catch (Exception e) {
            log.error("Error while starting jetty", e);
        }
    }

    public void blockUntilHttpServerShutdown() {
        try {
            if (jetty.isRunning()) {
                jetty.join();
            }
        } catch (Exception e) {
            log.error("Error while running jetty", e);
        } finally {
            try {
                jetty.stop();
            } catch (Exception e) {
                log.error("Error while stopping jetty", e);
            }
        }
    }
}
