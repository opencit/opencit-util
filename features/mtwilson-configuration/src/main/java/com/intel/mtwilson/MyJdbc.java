/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author jbuhacoff
 */
public class MyJdbc {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MyJdbc.class);
    private Configuration conf;
    private MyConfiguration config = new MyConfiguration(null);
    
    public MyJdbc(MyConfiguration config) {
        conf = new PropertiesConfiguration(config.getProperties("mtwilson.db.protocol", "mtwilson.db.driver", "mtwilson.db.host", "mtwilson.db.port", "mtwilson.db.schema", "mtwilson.db.user", "mtwilson.db.password"));
    }
    public MyJdbc(Configuration config) {
        conf = config;
    }
    public MyJdbc() throws IOException {
        this(ConfigurationFactory.getConfiguration());
    }
    
    public String url() {
        // if we just use the defaults then we lose an opportunity for clever auto-fill of properties (port/protocol/driver correlations)
        // that doesn't really belong in the configuration class
        // so first we try to fill in missing values:
        String protocol = conf.get("mtwilson.db.protocol", "");
        String driver = conf.get("mtwilson.db.driver", "");
        String port = conf.get("mtwilson.db.port", "");
        if( protocol.isEmpty() && !driver.isEmpty() ) {
            if( driver.contains("postgresql") ) {
                protocol = "postgresql";
            }
            if( driver.contains("mysql") ) {
                protocol = "mysql";
            }
        }
        if( protocol.isEmpty() && !port.isEmpty() ) {
            if( port.equals("5432") ) {
                protocol = "postgresql";
            }
            if( port.equals("3306") ) {
                protocol = "mysql";
            }
        }
        if( port.isEmpty() && !protocol.isEmpty() ) {
            if( protocol.equals("postgresql") ) {
                port = "5432";
            }
            if( protocol.equals("mysql") ) {
                port = "3306";
            }
        }
        // now if we are still missing information, use the defaults:
        if( protocol.isEmpty() ) {
            protocol = config.getDatabaseProtocol(); 
        }
        if( port.isEmpty() ) {
            port = port(); 
        }
        return String.format("jdbc:%s://%s:%s/%s", protocol, host(), port, schema());
    }
    
    public String host() {
        return conf.get("mtwilson.db.host", "127.0.0.1");
    }
    
    public String port() {
        if (conf.keys().contains("mtwilson.db.port")) {
            return conf.get("mtwilson.db.port", "5432");
        }
        if (conf.keys().contains("mountwilson.as.db.port")) {
            return conf.get("mountwilson.as.db.port", "5432");
        } 
        if (conf.keys().contains("mountwilson.ms.db.port")) {
            return conf.get("mountwilson.ms.db.port", "5432");
        } 
        if (conf.keys().contains("mtwilson.db.protocol")) {
            String protocol = conf.get("mtwilson.db.protocol", "");
            if (protocol.equals("postgresql")) {
                return "5432";
            }
            if (protocol.equals("mysql")) {
                return "3306";
            }
        }
        if (conf.keys().contains("mtwilson.db.driver")) {
            String port = conf.get("mtwilson.db.driver", "");
            if (port.equals("org.postgresql.Driver")) {
                return "5432";
            }
            if (port.equals("com.mysql.jdbc.Driver")) {
                return "3306";
            }
        }
        return "5432"; // 5432 is postgresql default, 3306 is mysql default
    }
    
    public String schema() {
        return conf.get("mtwilson.db.schema", "mw_as");
    }
    
    
    public String username() {
        return conf.get("mtwilson.db.user", ""); // removing default in mtwilson 1.2; was "root"
    }

    /** TODO:  check password vault first, then configuration */
    public String password() {
        return conf.get("mtwilson.db.password", conf.get("PGPASSWORD", "")); // removing default in mtwilson 1.2;  was "password";   // bug #733 
    }

    
    public String driver() {
        String protocol = conf.get("mtwilson.db.protocol", "");
        String driver = conf.get("mtwilson.db.driver", "");
        String port = conf.get("mtwilson.db.port", "");
        if( driver.isEmpty() && !protocol.isEmpty() ) {
            if( protocol.contains("postgresql") ) {
                driver = "org.postgresql.Driver";
            }
            if( protocol.contains("mysql") ) {
                driver = "com.mysql.jdbc.Driver";
            }
        }
        if( driver.isEmpty() && !port.isEmpty() ) {
            if( port.equals("5432") ) {
                driver = "org.postgresql.Driver";
            }
            if( port.equals("3306") ) {
                driver = "com.mysql.jdbc.Driver";
            }
        }
        return driver;
    }
    
    /**
     * Caller must close() the connection.
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    public Connection connection() throws ClassNotFoundException, SQLException {
        String driver = driver();
        log.debug("JDBC Driver: {}", driver);
        Class.forName(driver);
        Connection c = DriverManager.getConnection(url(), username(), password());
        return c;
    }
    
    /**
     * Use this static method to replace "My.jdbc().connection()" with
     * "MyJdbc.openConnection()" in code that would depend on mtwilson-my
     * but is not allowed due to its location in the architecture.
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    public static Connection openConnection() throws IOException, ClassNotFoundException, SQLException {
        MyJdbc jdbc = new MyJdbc();
        return jdbc.connection();
    }
}
