/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.login.token;

import com.intel.dcsg.cpg.configuration.Configuration;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 *
 * @author jbuhacoff
 */
public class LoginTokenUtils {
    public static String LOGIN_TOKEN_EXPIRES_MINUTES = "login.token.expires.minutes";
    public static String LOGIN_REQUIRES_TLS = "login.requires.tls";
    
    /**
     * 
     * @param <T>
     * @param collection
     * @return the first element returned by the iterator for the collection, or null if the collection is empty or null
     */
    public static <T> T getFirstElementFromCollection(Collection<T> collection) {
        if( collection != null ) {
            Iterator<T> iterator = collection.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            }
        }
        return null;
    }
    
    public static Date getExpirationDate(Date start, Configuration config) {
        Integer loginTokenExpiresMinutes = Integer.valueOf(config.get(LOGIN_TOKEN_EXPIRES_MINUTES, "30"));
        Calendar c = Calendar.getInstance();
        c.setTime(start);        
        c.add(Calendar.MINUTE, loginTokenExpiresMinutes);
        return c.getTime();
    }
    
}
