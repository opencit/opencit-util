/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.niarl.his.privacyca;

import java.io.ByteArrayInputStream;

/**
 *
 * @author zaaquino
 */
public class TpmsClockInfo {

    byte[] clock;	 /* time in milliseconds during which the TPM has been poweredThis structure element is used to report on the TPMs Clock value.The value of Clock shall be recorded in nonvolatile memory no less often than once per 222 milliseconds 69.9 minutes of TPM operation. The reference for the millisecond timer is the TPM oscillator.This value is reset to zero when the Storage Primary Seed is changed TPM2_Clear.This value may be advanced by TPM2_AdvanceClock.  */

    int resetCount;	 /* number of occurrences of TPM Reset since the last TPM2_Clear  */

    int restartCount;	 /* number of times that TPM2_Shutdown or _TPM_Hash_Start have occurred since the last TPM Reset or TPM2_Clear.  */

    byte safe = (byte) 0x00;	 /* no value of Clock greater than the current value of Clock has been previously reported by the TPM. Set to YES on TPM2_Clear.  */


    public TpmsClockInfo(ByteArrayInputStream source)
            throws TpmUtils.TpmUnsignedConversionException,
            TpmUtils.TpmBytestreamResouceException {
        clock = TpmUtils.getBytes(source, 8);
        resetCount = TpmUtils.getUINT32(source);
        restartCount = TpmUtils.getUINT32(source);
        safe = TpmUtils.getBytes(source, 1)[0];
    }
}
