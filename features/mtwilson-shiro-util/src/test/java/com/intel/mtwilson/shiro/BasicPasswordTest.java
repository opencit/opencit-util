/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class BasicPasswordTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BasicPasswordTest.class);

    public static class UsernamePassword {
        public String username = null;
        public String password = null;
    }
    
    // from PasswordAuthenticationFilter
    private UsernamePassword parseBasicPassword(String input) {
        UsernamePassword result = new UsernamePassword();
        result.username = "";
        result.password = "";
        int separatorIndex = input.indexOf(":");
        if( separatorIndex != -1 ) {
            result.username = input.substring(0, separatorIndex);
            if( separatorIndex < input.length() ) {
                result.password = input.substring(separatorIndex+1);
            }
        }
        return result;
    }
    
    private void test(String input, String usernameEquals, String passwordEquals) {
        UsernamePassword a = parseBasicPassword(input);
        assertEquals(usernameEquals, a.username);
        assertEquals(passwordEquals, a.password);
    }
    
    @Test
    public void testParseBasicPassword() {
        test("", "", "");
        test("username", "", "");
        test("username:", "username", "");
        test(":password", "", "password");
        test("username:password", "username", "password");
        test(":passwor:d", "", "passwor:d");
        test("username:passwor:d", "username", "passwor:d");
    }
}
