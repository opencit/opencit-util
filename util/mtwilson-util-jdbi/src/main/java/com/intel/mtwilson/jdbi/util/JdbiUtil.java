/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jdbi.util;

import org.skife.jdbi.v2.DBI;
import java.sql.Connection;

/**
 *
 * @author jbuhacoff
 */
public class JdbiUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JdbiUtil.class);

        // issue #4978: removing static instance
//    private static DBI dbi = null;
    
    public static DBI getDBI(Connection connection) {
        log.debug("JdbiUtil (mtwilson-util-jdbi) using connection: {}", connection);
//        if (dbi == null) {
//            dbi = new DBI(new ExistingConnectionFactory(connection));
//        }
        /*
        dbi = new DBI(new ExistingConnectionFactory(connection));   //previously created DBI object was causing issues in cit-3.0-beta2-sprint2; therefore forced creation of new object
        log.debug("JdbiUtil (mtwilson-util-jdbi) created new DBI instance: {}", dbi);
        return dbi;
        */
      // issue #4978: creating new DBI instance for each request
     log.debug("JdbiUtil (mtwilson-util-jdbi) created new DBI instance");
     return new DBI(new ExistingConnectionFactory(connection));
        
    }
}
