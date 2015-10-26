/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.feature.cmd;

import com.intel.dcsg.cpg.console.AbstractCommand;
import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.feature.JavaClasspathFactory;
import java.io.File;
import java.util.HashSet;

/**
 * Generates the java classpath for the application.
 * Without arguments, equivalent to:
 * <pre>
JARS=$(ls -1 $KMS_JAVA&#47;*.jar $KMS_HOME&#47;features&#47;*&#47;java&#47;*.jar)
CLASSPATH=$(echo $JARS | tr ' ' ':')
 * </pre>
 * 
 * With arguments, equivalent to the following where $1 is the feature id argument:
 * <pre>
JARS=$(ls -1 $KMS_HOME&#47;features&#47;$1&#47;java&#47;*.jar)
CLASSPATH=$(echo $JARS | tr ' ' ':')
 * </pre>
 * 
 * @author jbuhacoff
 */
public class JavaClasspath extends AbstractCommand implements Command {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JavaClasspath.class);
    
    @Override
    public void execute(String[] args) throws Exception {
        log.debug("args length: {}", args.length);
        
        if( args.length == 0 ) {
            // all application jars and feature jars in one list
            HashSet<File> jars = new HashSet<>();
            jars.addAll(JavaClasspathFactory.findApplicationJars());
            jars.addAll(JavaClasspathFactory.findAllFeatureJars());
            System.out.println(JavaClasspathFactory.toClasspath(jars));
        }
        else {
            // jar files from a specified feature
            HashSet<File> jars = new HashSet<>();
            jars.addAll(JavaClasspathFactory.findFeatureJars(args[0]));
            System.out.println(JavaClasspathFactory.toClasspath(jars));
        }
    }
    
}
