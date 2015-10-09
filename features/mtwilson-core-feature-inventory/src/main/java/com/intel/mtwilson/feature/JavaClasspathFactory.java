/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.feature;

import com.intel.dcsg.cpg.io.file.DirectoryFilter;
import com.intel.dcsg.cpg.io.file.FilenameEndsWithFilter;
import com.intel.mtwilson.Folders;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * Assembles a classpath comprised of all Java archives across all installed
 * features or for a specified feature.
 *
 * @author jbuhacoff
 */
public class JavaClasspathFactory {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JavaClasspathFactory.class);

    /**
     * This method returns a set of java archives found under the feature's
     * java folder.
     * 
     * @param featureId
     * @return 
     */
    public static Set<File> findFeatureJars(String featureId) {
        return findDirectoryJars(new File(Folders.features(featureId) + File.separator + "java"));        
    }
    
    /**
     * This method does not require a feature directory to include a feature.xml
     * file; all java archives found under the feature directories are included.
     * The set does NOT include the shared java archives found under the
     * application's java folder.
     *
     * @return set of all jars found under features/.../java subdirectories; may
     * be empty but never null
     */
    public static Set<File> findAllFeatureJars() {
        HashSet<File> jars = new HashSet<>();
        File featureDirectory = new File(Folders.features());
        File[] featureSubdirectories = featureDirectory.listFiles(new DirectoryFilter());
        if( featureSubdirectories != null ) {
        for (File featureSubdirectory : featureSubdirectories) {
            File featureJavaDirectory = featureSubdirectory.toPath().resolve("java").toFile();
            jars.addAll(findDirectoryJars(featureJavaDirectory));
        }
        }
        return jars;
    }

    /**
     * This method returns a set of java archives found under the application's
     * java folder.
     *
     * @return
     */
    public static Set<File> findApplicationJars() {
        return findDirectoryJars(new File(Folders.application() + File.separator + "java"));
    }

    
    /**
     * This method returns a set of java archives found under the specified
     * directory
     *
     * @return
     */
    public static Set<File> findDirectoryJars(File directory) {
        HashSet<File> jars = new HashSet<>();
        if (directory.exists() && directory.isDirectory()) {
            File[] jarFiles = directory.listFiles(new FilenameEndsWithFilter(".jar"));
            if( jarFiles != null ) {
            for (File jarFile : jarFiles) {
                log.debug("Adding jar: {}", jarFile.getName());
                jars.add(jarFile);
            }
            }
        }
        return jars;
    }
    
    public static String toClasspath(Set<File> jars) {
        return StringUtils.join(jars, File.pathSeparatorChar);
    }
}
