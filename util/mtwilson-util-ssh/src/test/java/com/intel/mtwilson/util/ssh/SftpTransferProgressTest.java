/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.ssh;

import com.intel.dcsg.cpg.performance.Progress;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashSet;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPFileTransfer;
import net.schmizz.sshj.xfer.FilePermission;
import net.schmizz.sshj.xfer.LocalFileFilter;
import net.schmizz.sshj.xfer.LocalSourceFile;
import net.schmizz.sshj.xfer.TransferListener;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class SftpTransferProgressTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SftpTransferProgressTest.class);

    // THIS IS AN INTEGRATION TEST,  COMMENTED OUT UNTIL THERE IS A FRAMEWORK
    // FOR INJECTING THE RIGHT RESOURCES (IN THIS CASE , THE SSH-ENABLED HOST
    // IP ADDRESS LIKE 192.168.1.100)
    @Test
    public void testLogSftpTransferProgress() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String host = "10.1.68.34";  // for example:  198.51.100.67
        int port = 22;
        String username = "root";
        String password = "P@ssw0rd";

        RemoteHostKeyDeferredVerifier hostKeyVerifier = new RemoteHostKeyDeferredVerifier();
        try (SSHClient ssh = new SSHClient()) {
            ssh.addHostKeyVerifier(hostKeyVerifier); // this accepts all remote public keys, then you have to verify the remote host key before continuing!!!
            ssh.setTimeout(3000); // in milliseconds, connection timeout 
            ssh.setConnectTimeout(5000); // in milliseconds, idle timeout after connected
            ssh.connect(host, port);
            ssh.authPassword(username, password);

            try (SFTPClient sftp = ssh.newSFTPClient()) {
                SFTPFileTransfer transfer = sftp.getFileTransfer();
                TransferListener listener = new LoggingTransferListener();
                transfer.setTransferListener(listener);
                LocalSourceFile file = new StringFile("foo", "abcdefghijklmnopqrstuvwxyz");
                transfer.upload(file, "/tmp/net.schmizz.sftp.upload.test");
            }

        } catch (IOException e) {
            log.debug("Connection failed", e); // we expect it to fail because we provided an empty password
        }
        log.debug("Remote host key for {}", hostKeyVerifier.getRemoteHostKey().getHost());
    }

    // THIS IS AN INTEGRATION TEST,  COMMENTED OUT UNTIL THERE IS A FRAMEWORK
    // FOR INJECTING THE RIGHT RESOURCES (IN THIS CASE , THE SSH-ENABLED HOST
    // IP ADDRESS LIKE 192.168.1.100)
    @Test
    public void testMonitorSftpTransferProgress() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String host = "10.1.68.34";  // for example:  198.51.100.67
        int port = 22;
        String username = "root";
        String password = "P@ssw0rd";

        RemoteHostKeyDeferredVerifier hostKeyVerifier = new RemoteHostKeyDeferredVerifier();
        try (SSHClient ssh = new SSHClient()) {
            ssh.addHostKeyVerifier(hostKeyVerifier); // this accepts all remote public keys, then you have to verify the remote host key before continuing!!!
            ssh.setTimeout(3000); // in milliseconds, connection timeout 
            ssh.setConnectTimeout(5000); // in milliseconds, idle timeout after connected
            ssh.connect(host, port);
            ssh.authPassword(username, password);

            try (SFTPClient sftp = ssh.newSFTPClient()) {
                SFTPFileTransfer transfer = sftp.getFileTransfer();
                ProgressTransferListener listener = new ProgressTransferListener();
                transfer.setTransferListener(listener);
                LocalSourceFile file = new StringFile("foo", "abcdefghijklmnopqrstuvwxyz");
                transfer.upload(file, "/tmp/net.schmizz.sftp.upload.test");
            }

        } catch (IOException e) {
            log.debug("Connection failed", e); // we expect it to fail because we provided an empty password
        }
        log.debug("Remote host key for {}", hostKeyVerifier.getRemoteHostKey().getHost());
    }
    
    public static class LoggingTransferListener implements TransferListener {

        @Override
        public TransferListener directory(String string) {
            log.debug("directory: {}", string);
            return this;
        }

        @Override
        public StreamCopier.Listener file(String filename, long size) {
            log.debug("file: {} length: {}", filename, size);
            return new LoggingFileTransferListener(filename, size);
        }
    }

    public static class ProgressTransferListener implements TransferListener {

        @Override
        public TransferListener directory(String string) {
            log.debug("directory: {}", string);
            return this;
        }

        @Override
        public StreamCopier.Listener file(String filename, long size) {
            log.debug("file: {} length: {}", filename, size);
            return new SftpTransferProgress(size);
        }
    }
    
    public static class LoggingFileTransferListener implements StreamCopier.Listener {
        private String filename;
        private long size;

        public LoggingFileTransferListener(String filename, long size) {
            this.filename = filename;
            this.size = size;
        }
        
        @Override
        public void reportProgress(long transferred) throws IOException {
            log.debug("progress: {} transferred: {} of size: {}", filename, transferred, size);
        }
        
    }

    public static class StringFile implements LocalSourceFile {

        private Charset UTF8 = Charset.forName("UTF-8");
        private String name;
        private String data;

        public StringFile(String name, String data) {
            this.data = data;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public long getLength() {
            return data.getBytes(UTF8).length;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data.getBytes(UTF8));
        }

        @Override
        public int getPermissions() throws IOException {
            HashSet<FilePermission> set = new HashSet<>();
            set.add(FilePermission.USR_R);
            set.add(FilePermission.USR_W);
            set.add(FilePermission.GRP_R);
            set.add(FilePermission.OTH_R);
            return FilePermission.toMask(set);
        }

        @Override
        public boolean isFile() {
            return true;
        }

        @Override
        public boolean isDirectory() {
            return false;
        }

        @Override
        public Iterable<? extends LocalSourceFile> getChildren(LocalFileFilter lff) throws IOException {
            ArrayList<LocalSourceFile> empty = new ArrayList<>();
            return empty;
        }

        @Override
        public boolean providesAtimeMtime() {
            return false;
        }

        @Override
        public long getLastAccessTime() throws IOException {
            return 0;
        }

        @Override
        public long getLastModifiedTime() throws IOException {
            return 0;
        }
    }
}
