package gov.niarl.his.privacyca;

import gov.niarl.his.privacyca.TpmUtils.TpmBytestreamResouceException;
import gov.niarl.his.privacyca.TpmUtils.TpmUnsignedConversionException;

import java.io.ByteArrayInputStream;

public class TpmCertifyKey20 {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TpmCertifyKey20.class);
    private byte [] magic = null;
    private byte [] type = null;
    private Tpm2bName tpm2bName = null;
    private Tpm2bData tpm2bData = null;
    private TpmsClockInfo tpmsClockInfo = null;
    private byte [] firmwareVersion = null;
    private TpmuAttest tpmuAttest = null;
    
    public TpmCertifyKey20() {
    }

    public TpmCertifyKey20(byte[] blob) throws TpmBytestreamResouceException, TpmUnsignedConversionException {
        ByteArrayInputStream bs = new ByteArrayInputStream(blob);        
        log.info("Reading byteblob  ...");
        magic = TpmUtils.getBytes(bs,4);
        log.debug("Read magic: {}",TpmUtils.byteArrayToHexString(magic));
        type = TpmUtils.getBytes(bs,2);
        log.debug("Read type: {}",TpmUtils.byteArrayToHexString(type));
        tpm2bName = new Tpm2bName(bs);
        tpm2bData = new Tpm2bData(bs);
        tpmsClockInfo = new TpmsClockInfo(bs);
        log.debug("Read tpmsClockInfo...");
        firmwareVersion = TpmUtils.getBytes(bs, 8);
        log.debug("Read firmwareVersion: {}",TpmUtils.byteArrayToHexString(firmwareVersion));
        tpmuAttest = new TpmuAttest(bs);
        log.debug("Read name digest buried in TPMU_ATTEST: {}",TpmUtils.byteArrayToHexString(tpmuAttest.getTpmsCertifyInfoBlob().getTpmtHa().getDigest()));
        log.debug(bs.toString());
    }
    public byte[] getMagic() {
        return magic;
    }
    public byte[] getType() {
        return type;
    }
    
    public Tpm2bName get2bName(){
        return tpm2bName;
    }
    
    public TpmuAttest getTpmuAttest(){
        return tpmuAttest;
    }
}
