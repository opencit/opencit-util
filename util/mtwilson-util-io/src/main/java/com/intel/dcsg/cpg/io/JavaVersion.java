/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.dcsg.cpg.io;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jbuhacoff
 */
public class JavaVersion {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JavaVersion.class);
    private static final Pattern SHORT_VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)");
    private static final Pattern LONG_VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)_(\\d+)");
    private static final JavaVersion INSTANCE = new JavaVersion();
    private final Integer major;
    private final Integer minor;
    private final Integer patch;
    private final Integer update;
    
    public JavaVersion() {
        this(System.getProperty("java.version")); // "1.8.0_92", etc
    }
    
    public JavaVersion(String version) {
        log.debug("Version: {}", version);
        Matcher matcher = LONG_VERSION_PATTERN.matcher(version);
        if( matcher.find() ) {
            major = Integer.valueOf(matcher.group(1));
            minor = Integer.valueOf(matcher.group(2));
            patch = Integer.valueOf(matcher.group(3));
            update = Integer.valueOf(matcher.group(4));
            log.debug("Major {} minor {} patch {} update {}", major, minor, patch, update);
        }
        else {
            patch = null;
            update = null;
            matcher = SHORT_VERSION_PATTERN.matcher(version);
            if( matcher.find() ) {
                major = Integer.valueOf(matcher.group(1));
                minor = Integer.valueOf(matcher.group(2));
                log.debug("Major {} minor {}", major, minor);
            }
            else {
                log.debug("Version string not matched: {}", matcher.toString());
                major = null;
                minor = null;
            }
        }
    }
    
    /**
     * 
     * @return the first number in "java.version", usually "1", or null if "java.version" was empty or could not be parsed
     */
    public Integer getMajor() {
        return major;
    }
    
    /**
     * 
     * @return the second number in "java.version", typically "1" through "8", or null if "java.version" was empty or could not be parsed
     */
    public Integer getMinor() {
        return minor;
    }
    
    /**
     * 
     * @return the third number in "java.version", typically "0", or null if "java.version" was empty or could not be parsed, or didn't have the patch number
     */
    public Integer getPatch() {
        return patch;
    }
    
    /**
     * 
     * @return the fourth number in "java.version", or null if "java.version" was empty or could not be parsed, or didn't have the update number
     */
    public Integer getUpdate() {
        return update;
    }
    
    /**
     * Compares the major & minor version, for example "1.8"
     * @param major
     * @param minor
     * @return 
     */
    public boolean isAtLeast(int major, int minor) {
        return ( this.major > major ) || ( this.major == major && this.minor >= minor );
    }

    /**
     * Compares the first three numbers, for example "1.8.0"
     * @param major
     * @param minor
     * @param patch
     * @return 
     */
    public boolean isAtLeast(int major, int minor, int patch) {
        return ( this.major > major ) || ( this.major == major && this.minor > minor ) || (this.major == major && this.minor == minor && this.patch >= patch );
    }

    /**
     * Compares the entire version string, for example "1.8.0_92"
     * @param major
     * @param minor
     * @param patch
     * @param update
     * @return 
     */
    public boolean isAtLeast(int major, int minor, int patch, int update) {
        return ( this.major > major ) || ( this.major == major && this.minor > minor ) || (this.major == major && this.minor == minor && this.patch > patch ) || ( this.major == major && this.minor == minor && this.patch == patch && this.update >= update );
    }
    
    public static JavaVersion runtime() { return INSTANCE; }
}
