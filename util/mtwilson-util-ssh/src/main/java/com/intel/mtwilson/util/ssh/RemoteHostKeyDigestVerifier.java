/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.ssh;

import com.intel.dcsg.cpg.crypto.digest.Digest;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
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
public class RemoteHostKeyDigestVerifier implements HostKeyVerifier {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RemoteHostKeyDigestVerifier.class);

    private String algorithm;
    private String hexDigest;

    public RemoteHostKeyDigestVerifier(String algorithm, String hexDigest) {
        this.algorithm = algorithm;
        this.hexDigest = hexDigest;
    }

    @Override
    public boolean verify(String host, int port, PublicKey publicKey) {
        if( publicKey instanceof RSAPublicKey ) {
            RSAPublicKey rsaPublicKey = (RSAPublicKey)publicKey;
            byte[] sshEncodedPublicKey = SshUtils.encodeSshRsaPublicKey(rsaPublicKey);
            String remoteHexDigest = Digest.algorithm(algorithm).digest(sshEncodedPublicKey).toHex();
            return hexDigest.equalsIgnoreCase(remoteHexDigest);
        }
        else {
            log.error("Unsupported public key class: {}", publicKey.getClass().getName());
            return false;
        }
    }

}
