/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author jbuhacoff
 */
public class JunitWebapp {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JunitWebapp.class);

    private static Server server;
    
    @BeforeClass
    public static void start() throws JettyStartException {
        server = new Server(8080);
        server.setStopAtShutdown(true);
        WebAppContext webAppContext = new WebAppContext();
//        webAppContext.setContextPath("/webapp");
        webAppContext.setResourceBase("src/main/webapp");       
//        webAppContext.setClassLoader(getClass().getClassLoader());
        server.setHandler(webAppContext);
        try {
        server.start();        
        }
        catch(Exception e) {
            throw new JettyStartException(String.valueOf(8080), e);
        }
    }
    
    @AfterClass
    public static void stop() throws JettyStopException {
        try {
        server.stop();
        }
        catch(Exception e) {
            throw new JettyStopException(String.valueOf(8080), e);
        }
    }
}
