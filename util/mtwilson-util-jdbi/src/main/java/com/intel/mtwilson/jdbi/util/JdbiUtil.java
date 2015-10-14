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
    private static DBI dbi = null;
    
    public static DBI getDBI(Connection connection) {
//        if (dbi == null) {
//            dbi = new DBI(new ExistingConnectionFactory(connection));
//        }
        dbi = new DBI(new ExistingConnectionFactory(connection));   //previously created DBI object was causing issues in cit-3.0-beta2-sprint2; therefore forced creation of new object
        return dbi;
    }
}
