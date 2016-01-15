/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.login.token;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.util.shiro.Login;
import com.intel.mtwilson.shiro.UsernameWithPermissions;
import com.intel.mtwilson.shiro.authc.token.MemoryTokenRealm;
import com.intel.mtwilson.shiro.authc.token.Token;
import com.intel.mtwilson.shiro.authc.token.TokenAuthenticationToken;
import com.intel.mtwilson.shiro.authc.token.TokenCredential;
import java.util.Collection;
import java.util.Date;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class LoginTokenTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoginTokenTest.class);

    @Test
    public void testLoginCreateToken() {
        // for testing purposes only, create an anonymous user with full permissions
        Configuration configuration = new PropertiesConfiguration();
        Login.user("*:*");
        
        Subject currentUser = SecurityUtils.getSubject();
        log.info("logged in as {}", currentUser.getPrincipal());
        PrincipalCollection principals = currentUser.getPrincipals();
        Collection<UsernameWithPermissions> usernames = principals.byType(UsernameWithPermissions.class);
        UsernameWithPermissions usernameWithPermissions = LoginTokenUtils.getFirstElementFromCollection(usernames);

        // create a new token
        String tokenValue = RandomUtil.randomBase64String(32); // new random token value
        Date notBefore = new Date(); // token is valid starting right now
        Date notAfter = LoginTokenUtils.getExpirationDate(notBefore, configuration);
        Integer used = 0; // new token
        Integer usedMax = null; // for logins we don't set a usage limit, just an expiration date
        TokenCredential tokenCredential = new TokenCredential(tokenValue, notBefore, notAfter, used, usedMax);
        
        MemoryTokenRealm.MemoryTokenDatabase database = MemoryTokenRealm.getDatabase();
        database.add(tokenCredential, usernameWithPermissions);
        
        // now logout
        currentUser.logout();
        
        // now login again using the token
        Token token = new Token(tokenValue);
        TokenAuthenticationToken loginToken = new TokenAuthenticationToken(token, "127.0.0.1");
        SecurityUtils.getSubject().login(loginToken);
        
        
    }
}
