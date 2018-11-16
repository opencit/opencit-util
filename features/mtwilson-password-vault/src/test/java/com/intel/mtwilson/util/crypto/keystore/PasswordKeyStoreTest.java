/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto.keystore;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class PasswordKeyStoreTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PasswordKeyStoreTest.class);

    @Test
    public void testPasswordKeyStore() throws Exception {
        String keystorePassword = RandomUtil.randomBase64String(16);
        Password testPassword = new Password(RandomUtil.randomBase64String(16).toCharArray());
        ByteArrayResource resource = new ByteArrayResource();
        // store the password in a keystore
        try(PasswordKeyStore keystore = new PasswordKeyStore("JCEKS", resource, keystorePassword.toCharArray())) {
            keystore.set("test.password", testPassword);
        }
        // now read the password back
        try(PasswordKeyStore keystore = new PasswordKeyStore("JCEKS", resource, keystorePassword.toCharArray())) {
            Password retrieved = keystore.get("test.password");
            assertArrayEquals(testPassword.toByteArray(), retrieved.toByteArray());
        }
    }
    
    @Test
    public void testEmptyPasswordKeyStore() throws Exception {
        String keystorePassword = RandomUtil.randomBase64String(16);
        ByteArrayResource resource = new ByteArrayResource();
        // create an empty keystore
        try(PasswordKeyStore keystore = new PasswordKeyStore("JCEKS", resource, keystorePassword.toCharArray())) {
            log.debug("about to close empty keystore");
        }
        // check its serialized size
        log.debug("empty keystore size: {}", resource.toByteArray().length);
        // now open the empty keystore and put a password in it
        Password testPassword = new Password(RandomUtil.randomBase64String(16).toCharArray());
        try(PasswordKeyStore keystore = new PasswordKeyStore("JCEKS", resource, keystorePassword.toCharArray())) {
            log.debug("opened the empty keystore, adding password"); 
            keystore.set("test.password", testPassword);
        }
        log.debug("keystore size with 1 password: {}", resource.toByteArray().length);
        // now read the password back
        try(PasswordKeyStore keystore = new PasswordKeyStore("JCEKS", resource, keystorePassword.toCharArray())) {
            Password retrieved = keystore.get("test.password");
            assertArrayEquals(testPassword.toByteArray(), retrieved.toByteArray());
        }
    }    
    
    /**
     * This test is for occasional interactive use; not needed for routine builds.
     * @throws Exception 
     */
//    @Test
    public void testMaxSizePasswordKeyStore() throws Exception {
        ByteArrayResource resource = new ByteArrayResource();
        String keystorePassword = RandomUtil.randomBase64String(16);
        try(PasswordKeyStore keystore = new PasswordKeyStore("JCEKS", resource, keystorePassword.toCharArray())) {
        for(int i=1; i<=Integer.MAX_VALUE; i++) {
            log.debug("Adding password {}", i);
            Password testPassword = new Password(RandomUtil.randomBase64String(16).toCharArray());
                keystore.set(String.format("test.%d", i), testPassword);
            }        
        }
    }
}
