/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.aes;

import com.intel.dcsg.cpg.crypto.Aes128;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.performance.Task;
import com.intel.dcsg.cpg.performance.report.PerformanceInfo;
import static com.intel.dcsg.cpg.performance.report.PerformanceUtil.measureSingleTask;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import javax.crypto.SecretKey;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class TestAesPerformance {
    private static final Logger log = LoggerFactory.getLogger(TestAesPerformance.class);
    private static final int max = 100000;
    private static final int RSA_SIZE = 1024;
    private static final int AES_SIZE = 128;
    
    @BeforeClass
    public static void startGrizzlyHttpServer() throws IOException {
    }

    @AfterClass
    public static void stopGrizzlyHttpServer() {
    }
    
    
    @Test
    public void testGenerateRsaPairs() throws Exception {
        PerformanceInfo info = measureSingleTask(new GenerateRsaPair(RSA_SIZE), max);
        printPerformanceInfo(info);
        long[] data = info.getData();
        for(int i=0; i<data.length; i++) {
            System.out.println(String.format("%d\t%d", i, data[i]));
        }
    }

    
    @Test
    public void testGenerateAesKeys() throws Exception {
        PerformanceInfo info = measureSingleTask(new GenerateAesKey(AES_SIZE), max);
        printPerformanceInfo(info);
        long[] data = info.getData();
        for(int i=0; i<data.length; i++) {
            System.out.println(String.format("%d\t%d", i, data[i]));
        }
    }

    
    @Test
    public void testAesEncryptPerformance() throws Exception {
        SecretKey key = Aes128.generateKey();
        PerformanceInfo info = measureSingleTask(new AesEncrypt(key), max);
        printPerformanceInfo(info);
        long[] data = info.getData();
        for(int i=0; i<data.length; i++) {
//            System.out.println(String.format("%d\t%d", i, data[i]));
        }
    }
    
    private static void printPerformanceInfo(PerformanceInfo info) {
        log.debug("Number of executions: {}", info.getData().length);
        log.debug("Average time: {}", info.getAverage());
        log.debug("Min time: {}", info.getMin());
        log.debug("Max time: {}", info.getMax());
    }
    
    private static class GenerateRsaPair extends Task {
        private final int keysize;
        private KeyPair keypair;
        public GenerateRsaPair(int keysize) {
            super(String.format("RSA %d", keysize)); // or can use the hashcode of the messsage as the id: String.valueOf(message.hashCode())
            this.keysize = keysize;
        }
        @Override
        public void execute() throws Exception {
            keypair = RsaUtil.generateRsaKeyPair(keysize);
//            log.debug(format("GenerateRsaPair[%s]", getId()));
        }
        public KeyPair getKeyPair() { return keypair; }
    }

    private static class RsaEncrypt extends Task {
        private final PrivateKey key;
        private byte[] input;
        private byte[] output;
        public RsaEncrypt(PrivateKey key) {
            super(String.format("RSA Encrypt")); // or can use the hashcode of the messsage as the id: String.valueOf(message.hashCode())
            this.input = new byte[128]; // TODO: random bytes...
            this.key = key;
        }
        @Override
        public void execute() throws Exception {
//            output = RsaUtil.(message);
//            log.debug(format("GenerateRsaPair[%s]", getId()));
        }
        public byte[] getOutput() { return output; }
    }
    
    /**
     * Java performance without AES-NI... on HP EliteBook 8560w running Intel Core i5-2560M CPU @ 2.6GHz 
     * 10k encryptions in about 3 seconds,
     * 100k encryptions in about 30 seconds
     */
    private static class GenerateAesKey extends Task {
        private final int keysize;
        private SecretKey key;
        public GenerateAesKey(int keysize) {
            super(String.format("AES %d", keysize)); // or can use the hashcode of the messsage as the id: String.valueOf(message.hashCode())
            this.keysize = keysize;
        }
        @Override
        public void execute() throws Exception {
            key = Aes128.generateKey();
//            log.debug(format("GenerateRsaPair[%s]", getId()));
        }
        public SecretKey getKey() { return key; }
    }
    
    private static class AesEncrypt extends Task {
        private final Aes128 cipher;
        private byte[] input;
        private byte[] output;
        public AesEncrypt(SecretKey key) throws CryptographyException {
            super(String.format("AES Encrypt")); // or can use the hashcode of the messsage as the id: String.valueOf(message.hashCode())
            this.cipher = new Aes128(key);
            this.input = new byte[128]; // TODO: random bytes...
        }
        @Override
        public void execute() throws Exception {
            output = cipher.encrypt(input);
        }
        public byte[] getOutput() { return output; }
    }
    
}
