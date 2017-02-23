/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.niarl.his.privacyca;

import gov.niarl.his.privacyca.TpmUtils.TpmBytestreamResouceException;
import gov.niarl.his.privacyca.TpmUtils.TpmUnsignedConversionException;
import java.io.ByteArrayInputStream;

/**
 *
 * @author zaaquino
 */
public class TpmtHA {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TpmtHA.class);
    short hashAlg;	/*0x000b = Sha256*/ /* selector of the hash contained in the digest that implies the size of the digestNOTE	
     The leading + on the type indicates that this structure should pass an indication to the unmarshaling function 
     for TPMI_ALG_HASH so that TPM_ALG_NULL will be allowed if a use of a TPMT_HA allows TPM_ALG_NULL.   */


    byte[] digest;     /* the digest data   */


    public TpmtHA(short size, byte[] blob) throws TpmBytestreamResouceException, TpmUnsignedConversionException {
        ByteArrayInputStream bs = new ByteArrayInputStream(blob);
        hashAlg = TpmUtils.getUINT16(bs);
        digest = TpmUtils.getBytes(bs, (int) size - 2);//subracting the hashAlg size, so we read only the remaining 32 bytes
    }

    public short getHashAlg() {
        return hashAlg;
    }

    public byte[] getDigest() {
        return digest;
    }
}
