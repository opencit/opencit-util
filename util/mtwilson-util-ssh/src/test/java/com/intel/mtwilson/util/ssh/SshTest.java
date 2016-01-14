/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.ssh;

import java.io.IOException;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class SshTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SshTest.class);

    private void connect(String host, int port, String username, String password, String fingerprint) throws IOException {
        RemoteHostKeyDigestVerifier hostKeyVerifier1 = new RemoteHostKeyDigestVerifier(host, port, "MD5", fingerprint);
        try (SSHClient ssh = new SSHClient()) {
            ssh.addHostKeyVerifier(hostKeyVerifier1); // this accepts all remote public keys, then you have to verify the remote host key before continuing!!!
            ssh.setTimeout(3000); // in milliseconds, connection timeout 
            ssh.setConnectTimeout(5000); // in milliseconds, idle timeout after connected
            ssh.connect(host, port);
            ssh.authPassword(username, password);
            log.debug("Connected to host {}", host);
//            try(Session session = ssh.startSession()) {
                
//            session.join();
//            }
        }
    }
    
    
    @Test
    public void testSsh() throws IOException {
        int port = 22;
        String username = "root";
        String password = "P@ssw0rd";
        connect("10.1.68.34", port, username, password, "22952a72e24194f208200e76fd3900da");
        connect("10.1.68.31", port, username, password, "a8334b9235fb0018557b41d8350054a0");

        
    }
}
