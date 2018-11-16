/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.ssh;

import java.security.PublicKey;

/**
 *
 * @author jbuhacoff
 */
public class RemoteHostKey {
    private String host;
    private Integer port;
    private PublicKey publicKey;

    public RemoteHostKey() {
    }

    public RemoteHostKey(String host, Integer port, PublicKey publicKey) {
        this.host = host;
        this.port = port;
        this.publicKey = publicKey;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }
    
    
}
