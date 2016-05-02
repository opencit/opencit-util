/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.login.token.cmd;

import com.intel.dcsg.cpg.console.Command;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.shiro.UsernameWithPermissions;
import com.intel.mtwilson.shiro.authc.token.FileTokenRealm.FileTokenDatabase;
import com.intel.mtwilson.shiro.authc.token.MemoryTokenRealm.TokenRecord;
import com.intel.mtwilson.shiro.authc.token.TokenCredential;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Creates and stores a limited-term token, then connects to the running
 * server to inform it to load tokens from storage into memory (this is 
 * to avoid making a change to in-memory token mechanism at this time,
 * but in the future it would probably be better to allow that feature to
 * check memory first, then disk, before denying a request).
 * 
 * Note: this command requires the server to be running
 *
 * @author jbuhacoff
 */
public class LoginToken implements Command {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoginToken.class);
    private Configuration options = null;

    /**
     * Options:
     * username - username to be associated with the token (does not affect permissions)
     * permissions - comma-separated list of permissions to be associated with the token;   this is required and there is no default, if this is omitted the token will not have any associated permissions and will be useless
     * max - number of times the token can be used, optional
     * time - duration after which the token expires, optional.   can specify a unit of 'm'inutes, 'h'ours, 'd'ays, or 'w'eeks, like  "30m", "2h", "5d", "1w". 
     * @param options 
     */
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    @Override
    public void execute(String[] args) throws Exception {
        if (options == null) {
            options = new PropertiesConfiguration();
        }
        
        String username = options.getString("username", null);
        
        HashSet<String> permissions = new HashSet<>();
        if( options.containsKey("permissions") ) {
            String[] permissionArray = options.getString("permissions", "").split(",");
            permissions.addAll(Arrays.asList(permissionArray));
        }
        
        UsernameWithPermissions usernameWithPermissions = new UsernameWithPermissions(username, permissions);
        
        Date notBefore = new Date(); // token is valid starting right now
        Date notAfter = null;
        if( options.containsKey("time") ) {
            notAfter = getExpirationDate(notBefore, options.getString("time"));
        }
        
        Integer used = 0; // new token
        Integer usedMax = null;
        if( options.containsKey("max")) {
            usedMax = Integer.valueOf(options.getString("max"));
        }
        
        String tokenValue = RandomUtil.randomBase64String(32); // new random token value
        TokenCredential tokenCredential = new TokenCredential(tokenValue, notBefore, notAfter, used, usedMax);
        
        TokenRecord tokenRecord = new TokenRecord(tokenCredential, usernameWithPermissions);
        FileTokenDatabase fileTokenDatabase = new FileTokenDatabase();        
        fileTokenDatabase.add(tokenRecord);

        com.intel.dcsg.cpg.configuration.Configuration configuration = ConfigurationFactory.getConfiguration();
        String baseurl = configuration.get("endpoint.url", "http://localhost");
        String url = String.format("%s?authorization_token=%s", baseurl, tokenValue);
        System.out.println(url);

    }
    
    public Date getExpirationDate(Date start, String expirationText) {
        Integer loginTokenExpiresMinutes;
        if( expirationText.endsWith("m") ) {
            loginTokenExpiresMinutes = Integer.valueOf(expirationText.substring(0, expirationText.length()-1));
        }
        else if( expirationText.endsWith("h") ) {
            loginTokenExpiresMinutes = Integer.valueOf(expirationText.substring(0, expirationText.length()-1)) * 60;
        }
        else if( expirationText.endsWith("d") ) {
            loginTokenExpiresMinutes = Integer.valueOf(expirationText.substring(0, expirationText.length()-1)) * 60 * 24;
        }
        else if( expirationText.endsWith("w") ) {
            loginTokenExpiresMinutes = Integer.valueOf(expirationText.substring(0, expirationText.length()-1)) * 60 * 24 * 7;
        }
        else {
            throw new IllegalArgumentException("time");
        }
        Calendar c = Calendar.getInstance();
        c.setTime(start);        
        c.add(Calendar.MINUTE, loginTokenExpiresMinutes);
        return c.getTime();
    }
    
}
