/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.ssh;

import java.security.PublicKey;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;

/**
 * This verifier always succeeds, so the connection is made, but it saves
 * the remote key information so the user can verify it later and then
 * (hopefully) terminate the connection if the key did not check out.
 * This is useful in conjunction with a UI which displays the remote host
 * key and asks the user to verify. We need to save that key for the UI
 * to display, without driving the UI from here.
 * You can either provide a reference to your own RemoteHostKey object which
 * will be populated, or you can use the non-arg constructor and then call
 * getRemoteHostKey() to get a new populated object.
 * 
 * @author jbuhacoff
 */
public class RemoteHostKeyDeferredVerifier implements HostKeyVerifier {
    private RemoteHostKey remoteHostKey;

    public RemoteHostKeyDeferredVerifier() {
        this.remoteHostKey = new RemoteHostKey();
    }

    public RemoteHostKeyDeferredVerifier(RemoteHostKey remoteHostKey) {
        this.remoteHostKey = remoteHostKey;
    }

    @Override
    public boolean verify(String host, int port, PublicKey publicKey) {
        remoteHostKey.setHost(host);
        remoteHostKey.setPort(port);
        remoteHostKey.setPublicKey(publicKey);
        return true;
    }

    public RemoteHostKey getRemoteHostKey() {
        return remoteHostKey;
    }
    
}
