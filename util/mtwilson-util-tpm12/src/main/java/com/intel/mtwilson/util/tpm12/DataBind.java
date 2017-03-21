/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.tpm12;

import com.intel.dcsg.cpg.io.ByteArray;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 *
 * @author jbuhacoff
 */
public class DataBind {

    /**
     * From trousers-0.3.13/src/include/tss/tpm.h:
     *
     * <pre>
     * typedef struct tdTPM_STRUCT_VER
     * {
     * BYTE   major;
     * BYTE   minor;
     * BYTE   revMajor;
     * BYTE   revMinor;
     * } TPM_STRUCT_VER;
     * </pre>
     *
     * From trousers-0.3.13/src/include/tss/compat11b.h:
     * <pre>
     * typedef TPM_STRUCT_VER TCPA_VERSION;
     * </pre>
     *
     * From trousers-0.3.13/src/tspi/spi_utils.c:
     * <pre>
     * TSS_VERSION VERSION_1_1 = { 1, 1, 0, 0 };
     * </pre>
     *
     * From trousers-0.3.13/src/include/tss/tss_structs.h:
     * <pre>
     * typedef struct tdTSS_VERSION
     * {
     * BYTE   bMajor;
     * BYTE   bMinor;
     * BYTE   bRevMajor;
     * BYTE   bRevMinor;
     * } TSS_VERSION;
     * </pre>
     */
    protected static class TpmVersion {
        public final byte major;
        public final byte minor;
        public final byte revMajor;
        public final byte revMinor;

        public TpmVersion(byte major, byte minor, byte revMajor, byte revMinor) {
            this.major = major;
            this.minor = minor;
            this.revMajor = revMajor;
            this.revMinor = revMinor;
        }
        public byte[] toByteArray() { return new byte[] { major, minor, revMajor, revMinor }; }
    }
    protected static final TpmVersion VERSION_1_1 = new TpmVersion( (byte)0x01, (byte)0x01, (byte)0x00, (byte)0x00 );

    /**
     * From trousers-0.3.13/src/include/tss/compat11b.h and tss/tpm.h:
     * <pre>
     * typedef BYTE TPM_PAYLOAD_TYPE;
     * #define TPM_PT_BIND ((BYTE)0x02)
     * #define TPM_PT_SEAL ((BYTE)0x05)
     * </pre>
     * 
     * NOTE: you can bind to a remote TPM's public key, but only a TPM can
     * seal data with PCR values for itself.
     */
    protected static enum TpmPayloadType {
        TPM_PT_BIND( (byte)0x02 ),
        TPM_PT_SEAL( (byte)0x05 );
        public final byte value;
        private TpmPayloadType(byte value) { this.value = value; }
        public byte[] toByteArray() { return new byte[] { value }; }
    }


    public static enum EncScheme {
        TPM_ES_RSAESPKCSv15((int) 0x0002),
        TPM_ES_RSAESOAEP_SHA1_MGF((int) 0x0003),
        TPM_ALG_RSAES((int) 0x0015),
        TPM_ALG_OAEP((int) 0x0017);

        public final int value;

        private EncScheme(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        private static Map<Integer, EncScheme> map = new HashMap<>();

        static {
            for (EncScheme encScheme : EncScheme.values()) {
                map.put(encScheme.value, encScheme);
            }
        }
        public static EncScheme valueOf(int value) {
            return map.get(value);
        }

        @Override
        public String toString() {
            return "EncScheme{" +
                    "value=" + value +
                    '}';
        }
    }




    /**
     * From trousers-0.3.13/src/include/tss/tpm.h:
     * <pre>
     * typedef struct tdTPM_BOUND_DATA
     * {
     * TPM_STRUCT_VER   ver;
     * TPM_PAYLOAD_TYPE payload;
     * BYTE            *payloadData; // length is implied
     * } TPM_BOUND_DATA;
     *
     * typedef TPM_BOUND_DATA TCPA_BOUND_DATA
     * </pre>
     * 
     */
    protected static class TpmBoundData {
        public TpmVersion ver;
        public TpmPayloadType payload;
        public byte[] payloadData;

        public TpmBoundData(TpmVersion ver, TpmPayloadType payload, byte[] payloadData) {
            this.ver = ver;
            this.payload = payload;
            this.payloadData = payloadData;
        }
        
        public byte[] toByteArray() { return ByteArray.concat(ver.toByteArray(), payload.toByteArray(), payloadData); }
    }
    

    protected static PSource getPSource() {
        return new PSource.PSpecified(new byte[] { 'T', 'C', 'P', 'A' });
    }
    protected static OAEPParameterSpec getOAEPParameterSpec() {
        return new OAEPParameterSpec("SHA-1", "MGF1", MGF1ParameterSpec.SHA1, getPSource());
    }
    /*
    public static RSAPadding getRSAPadding() {
        
    }
    * */
    protected static Cipher getCipher(PublicKey publicKey, EncScheme encScheme) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        Cipher cipher = null;
        if (encScheme == EncScheme.TPM_ES_RSAESOAEP_SHA1_MGF) {
//            Cipher cipher = Cipher.getInstance("RSA"); // throws NoSuchAlgorithmException, NoSuchPaddingException
            Provider bc = new BouncyCastleProvider();
//        Security.addProvider(new BouncyCastleProvider());        // required because without it, next line throws java.security.NoSuchAlgorithmException: Cannot find any provider supporting RSA/ECB/OAEP

//            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEP", bc); // commented out because when specifying OAEP it goes to a list of pre-defined ones, instead of using the parameter spec provided below, so because "OAEP" itself is not in the bouncycastle list it rhwos:   javax.crypto.NoSuchPaddingException: OAEP unavailable with RSA
            cipher = Cipher.getInstance("RSA", bc); //
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, getOAEPParameterSpec()); // throws InvalidKeyException, InvalidAlgorithmParameterException
        } else if (encScheme == EncScheme.TPM_ES_RSAESPKCSv15 || encScheme==EncScheme.TPM_ALG_RSAES) {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        }
        return cipher;
    }


    protected static Cipher getCipher(PublicKey publicKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        return getCipher(publicKey, EncScheme.TPM_ES_RSAESOAEP_SHA1_MGF);
    }


    public static byte[] bind(byte[] plaintext, TpmPublicKey tpmPublicKey, EncScheme encScheme) throws GeneralSecurityException {
        return bind(plaintext, tpmPublicKey.toPublicKey(), encScheme);
    }
    
    public static byte[] bind(byte[] plaintext, PublicKey publicKey, EncScheme encScheme) throws GeneralSecurityException {
        Cipher cipher = getCipher(publicKey, encScheme); // throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException
//        byte[] encrypted = cipher.wrap(secretKey); // throws IllegalBlockSizeException
        if(cipher == null)
            throw new GeneralSecurityException("cipher is null, unable to continue");
        byte[] encrypted;
        if (encScheme==EncScheme.TPM_ALG_RSAES) { //tpm2.0 defines TPM_ALG_RSAES, which does not seem needs the tpm version and PT_BIND padding
            encrypted = cipher.doFinal(plaintext); // throws BadPaddingException            
        }
        else { //tpm1.2 needs the tpm version and TPM_PT_BIND patch for binding
            encrypted = cipher.doFinal(new DataBind.TpmBoundData(DataBind.VERSION_1_1, DataBind.TpmPayloadType.TPM_PT_BIND, plaintext).toByteArray()); // throws BadPaddingException
        }
        return encrypted;
    }

    public static byte[] bind(byte[] plaintext, TpmPublicKey tpmPublicKey) throws GeneralSecurityException {
        return bind(plaintext, tpmPublicKey.toPublicKey(), EncScheme.TPM_ES_RSAESOAEP_SHA1_MGF);
    }

    public static byte[] bind(byte[] plaintext, PublicKey publicKey) throws GeneralSecurityException {
        Cipher cipher = getCipher(publicKey, EncScheme.TPM_ES_RSAESOAEP_SHA1_MGF); // throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException
//        byte[] encrypted = cipher.wrap(secretKey); // throws IllegalBlockSizeException
        if(cipher == null)
            throw new GeneralSecurityException("cipher is null, unable to continue");
        byte[] encrypted = cipher.doFinal(new DataBind.TpmBoundData(DataBind.VERSION_1_1, DataBind.TpmPayloadType.TPM_PT_BIND, plaintext).toByteArray()); // throws BadPaddingException
        return encrypted;
    }

    
}

