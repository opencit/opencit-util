/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.grizzly;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
//import org.glassfish.grizzly.http.server.HttpServer;
//import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class TestDekRequest {
    private static final Logger log = LoggerFactory.getLogger(TestDekRequest.class);
    private static String url = "http://localhost:9999/v1";
//    private static HttpServer server;

    @BeforeClass
    public static void startGrizzlyHttpServer() throws IOException {
        final Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("com.sun.jersey.config.property.packages",
                "com.intel.mh.server.http.jersey");
        ResourceConfig rc = new ResourceConfig().packages("com.intel.mh.server.http.jersey");
//        server = GrizzlyHttpServerFactory.createHttpServer(URI.create(url), rc);
            
        log.info("WADL: {}/application.wadl", url);
    }

    @AfterClass
    public static void stopGrizzlyHttpServer() {
//        server.stop();
    }
    
    
//    @Test
    public void testPostDek() throws IOException {
        String aik = "pretend this is an aik public key blob";
//        HttpGet request = new HttpGet(url+"/data-encryption-key/request/dek1");
        HttpPost request = new HttpPost(url+"/data-encryption-key/request/test-dek-1");
//        request.setEntity(new StringEntity(aik, ContentType.create("text/plain", "UTF-8")));
        request.setEntity(new ByteArrayEntity(aik.getBytes(), ContentType.create("application/octet-stream", "UTF-8")));
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(request);
        System.out.println(response.getStatusLine());
        System.out.println(IOUtils.toString(response.getEntity().getContent()));
        httpClient.getConnectionManager().shutdown();
        
    }
}

