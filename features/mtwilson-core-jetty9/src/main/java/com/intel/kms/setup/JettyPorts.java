/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.setup;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.ValveConfiguration;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.setup.AbstractSetupTask;

/**
 *
 * @author jbuhacoff
 */
public class JettyPorts extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JettyPorts.class);

    // configuration keys
    public static final String JETTY_PORT = "jetty.port";
    public static final String JETTY_SECURE_PORT = "jetty.secure.port";
    
    private Configuration file;
    private Configuration environment;
    private String[] settings;

    public JettyPorts() {
        super();
        settings = new String[] { JETTY_PORT, JETTY_SECURE_PORT };
    }
    
    @Override
    protected void configure() throws Exception {
        environment = getConfiguration(); // not just environment but also other sources as configured by setup manager //new KeyTransformerConfiguration(new AllCapsNamingStrategy(), new EnvironmentConfiguration());
        if( environment instanceof ValveConfiguration ) {
            file = ((ValveConfiguration)environment).getWriteTo();
        }
        else {
            file = ConfigurationFactory.getConfiguration();
        }
    }

    @Override
    protected void validate() throws Exception {
        for(String setting : settings) {
            String env = environment.get(setting);
            log.debug("Environment value for {} is {}", setting, env);
            if( env == null ) { continue; }
            String conf = file.get(setting);
            log.debug("Configured value for {} is {}", setting, conf);
            if(  conf == null ||  !env.equals(conf) ) {
                validation(String.format("Configured value %s for %s does not match environment value %s", conf, setting, env));
            }
        }
    }

    @Override
    protected void execute() throws Exception {
        for(String setting : settings) {
            String env = environment.get(setting);
            if( env == null ) { continue; }
            String conf = file.get(setting);
            if(  conf == null ||  !env.equals(conf) ) {
                getConfiguration().set(setting, env);
            }
        }
    }
    
}
