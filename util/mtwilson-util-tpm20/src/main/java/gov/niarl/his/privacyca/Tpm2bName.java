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
public class Tpm2bName {

    private short size;
    private byte[] name;
    /*============================================================================
     Parameter                       Type                    Description
     size                            UINT16                  size of the Name structure
     name[size]{:sizeof(TPMU_NAME)}  BYTE                    the Name structure
     //==============================================================================*/

    public Tpm2bName(ByteArrayInputStream source)
            throws TpmUtils.TpmUnsignedConversionException,
            TpmUtils.TpmBytestreamResouceException {
        size = TpmUtils.getUINT16(source);
        name = TpmUtils.getBytes(source, (int) size);
    }

    public short getSize() {
        return size;
    }

    public byte[] getName() {
        return name;
    }

    public TpmtHA getTpmtHa() throws TpmUtils.TpmBytestreamResouceException, TpmUtils.TpmUnsignedConversionException {
        return new TpmtHA(size, name);
    }

}
