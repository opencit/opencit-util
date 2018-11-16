/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration.cmd;

import com.intel.dcsg.cpg.console.InteractiveCommand;
import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.mtwilson.Environment;
import com.intel.mtwilson.MyConfiguration;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
/**
 *
 * @author jbuhacoff
 */
public class ExportConfig extends InteractiveCommand {
    private final String USAGE = "Usage: export-config <outfile|--in=infile|--out=outfile|--stdout> [--env-password=PASSWORD_VAR]";

    @Override
    public void execute(String[] args) throws Exception {


        String password;
        if( options != null && options.containsKey("env-password") ) {
            password = getExistingPassword("the Server Configuration File", "env-password");
        }
        else {
            // gets environment variable MTWILSON_PASSWORD, TRUSTAGENT_PASSWORD, KMS_PASSWORD, etc.
            password = Environment.get("PASSWORD");
            if( password == null ) {
                throw new IllegalArgumentException(USAGE);
            }
        }

        File inputFile;
        if( options != null && options.containsKey("in") ) {
            inputFile = new File(options.getString("in"));
        } else {
            MyConfiguration config = new MyConfiguration();
            inputFile = config.getConfigurationFile();
        }
        FileResource in = new FileResource(inputFile);
        
        // Bug:4793 - Since we were already opening the file output stream and then making the call to the decryption function,
        // the decryption function was not able to read the contents of the encrypted file if the same file was used to write 
        // back the decrytped contents. So, we are decrypting the contents first and writing to the file.
        String decryptedContent = export(in, password);

        if( options != null && options.containsKey("out") ) {
            String filename = options.getString("out");
            try(FileOutputStream out = new FileOutputStream(new File(filename))) {
                IOUtils.write(decryptedContent, out);
            }
        } else if( options != null && options.getBoolean("stdout", false) ) {
            log.debug("Output filename not provided; exporting to stdout");
            IOUtils.write(decryptedContent, System.out);
        } else if( args.length == 1 ) {
            String filename = args[0];
            try(FileOutputStream out = new FileOutputStream(new File(filename))) {
                IOUtils.write(decryptedContent, out);
            }
        } else {
            throw new IllegalArgumentException(USAGE);
        }
    }

    public String export(FileResource in, String password) throws IOException {
        PasswordEncryptedFile encryptedFile = new PasswordEncryptedFile(in, password);
        String content = encryptedFile.loadString();
        return content;
    }
}
