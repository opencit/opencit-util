/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.setup;

import com.intel.kms.jetty9.StartHttpServer;
import com.intel.kms.setup.faults.IniEntryMismatch;
import com.intel.kms.setup.faults.IniEntryNotFound;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.setup.faults.FileNotFound;
import java.io.File;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.shiro.config.Ini;

/**
 * Automatically updates the "ssl.port" setting in the "shiro.ini" file to
 * match the "jetty.secure.port" setting in the configuration.
 * 
 * NOTE: this setup task is similar to "UpdateSslPort" in mtwilson-shiro-setup
 * but instead of calling getMtWilsonURL from MyConfiguration, this task uses
 * the provided configuration and looks for the "jetty.secure.port" property.
 * Also this setup task doesn't automatically enable ssl.enabled if it's 
 * disabled, since it's enabled out of the box.
 * 
 * @author jbuhacoff
 */
public class ShiroSslPort extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShiroSslPort.class);

    private File shiroIniFile;
    private Integer httpsPort;
    
    @Override
    protected void configure() throws Exception {
        httpsPort = Integer.valueOf(getConfiguration().get(JettyPorts.JETTY_SECURE_PORT, "443"));
        shiroIniFile = new File(Folders.configuration() + File.separator + "shiro.ini");
        if (!shiroIniFile.exists()) {
            configuration(new FileNotFound(shiroIniFile.getAbsolutePath())); // "File not found: shiro.ini"
        }
    }

    @Override
    protected void validate() throws Exception {
        log.debug("jetty.secure.port = {}", httpsPort);
        // now if it's set, check the shiro.ini file to see if it matches
        Ini shiroIni = new Ini();
        shiroIni.load(FileUtils.readFileToString(shiroIniFile));
        String portNumber = shiroIni.getSectionProperty("main","ssl.port",null);
        if( portNumber == null ) {
            validation(new IniEntryNotFound(shiroIniFile.getAbsolutePath(), "main", "ssl.port")); // shiro.ini [main] is missing ssl.port setting
        }
        else {
            Integer shiroPort = Integer.valueOf(portNumber);
            if( !shiroPort.equals(httpsPort) ) {
                log.debug("shiro.ini [main] ssl.port = {}", shiroPort);
                validation(new IniEntryMismatch(shiroIniFile.getAbsolutePath(), "main", "ssl.port", httpsPort.toString())); // "shiro.ini [main] ssl.port is not up to date"
            }
        }
    }

    @Override
    protected void execute() throws Exception {
        Ini shiroIni = new Ini();
        shiroIni.load(FileUtils.readFileToString(shiroIniFile));
        shiroIni.setSectionProperty("main","ssl.port",String.valueOf(httpsPort));
        StringBuilder newShiroConfig = new StringBuilder();
        Collection<Ini.Section> sections = shiroIni.getSections();
        for (Ini.Section section : sections) {
            newShiroConfig.append(String.format("[%s]\r\n",section.getName()));
            for (String sectionKey : section.keySet()) {
                newShiroConfig.append(String.format("%s = %s\r\n",sectionKey,section.get(sectionKey)));
            }
        }
        
        FileUtils.writeStringToFile(shiroIniFile, newShiroConfig.toString());
    }
    
}
