/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.console.cmd;

import com.intel.dcsg.cpg.console.AbstractCommand;
import com.intel.kms.jetty9.StartHttpServer;

/**
 *
 * @author jbuhacoff
 */
public class JettyStart extends AbstractCommand {

    @Override
    public void execute(String[] args) throws Exception {
        StartHttpServer server = new StartHttpServer();
        server.run();
        server.blockUntilHttpServerShutdown();
    }
    
}
