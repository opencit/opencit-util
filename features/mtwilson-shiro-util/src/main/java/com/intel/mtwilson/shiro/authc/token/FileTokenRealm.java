/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
import com.intel.mtwilson.shiro.Username;
import com.intel.mtwilson.shiro.UsernameWithPermissions;
import com.intel.mtwilson.shiro.authc.token.MemoryTokenRealm.TokenRecord;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

/**
 * A token implementation that looks for tokens on disk. Tokens that are found
 * on disk are cached in memory only if they are "not-after" tokens. The
 * "use-max" tokens are not cached in memory because if the server is shut down
 * and their state has not been written to disk, the client could reuse them
 * later when the server restarts and their outdated state is reloaded from
 * disk.
 *
 * @author jbuhacoff
 */
public class FileTokenRealm extends AuthorizingRealm {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileTokenRealm.class);
    private static volatile FileTokenDatabase database = new FileTokenDatabase();

    public static FileTokenDatabase getDatabase() {
        return database;
    }

    public FileTokenRealm() {
        super();
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof TokenAuthenticationToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection pc) {
        if (pc == null) {
            throw new AuthorizationException("Principal must be provided");
        }
        SimpleAuthorizationInfo authzInfo = new SimpleAuthorizationInfo();
        for (String realmName : pc.getRealmNames()) {
            log.debug("doGetAuthorizationInfo for realm: {}", realmName);
        }
        try {
            Collection<Token> subjects = pc.byType(Token.class);
            for (Token subject : subjects) {
                log.debug("doGetAuthorizationInfo for token: {}", subject.getValue());
                TokenRecord existing = database.findByTokenValue(subject.getValue());
                if (existing == null) {
                    log.debug("doGetAuthorizationInfo token not found: {}", subject.getValue());
                    continue;
                }
                log.debug("doGetAuthorizationInfo found permissions: {}", existing.getPermissions());
                for (String permission : existing.getPermissions()) {
                    log.debug("doGetAuthorizationInfo adding permision '{}'", permission);
                    authzInfo.addStringPermission(permission);
                }
            }
        } catch (Exception e) {
            log.debug("doGetAuthorizationInfo error", e);
            throw new AuthenticationException("Internal server error", e);
        }

        return authzInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        TokenAuthenticationToken subjectToken = (TokenAuthenticationToken) token;
        String tokenValue = subjectToken.getToken().getValue();
        if (tokenValue == null) {
            log.debug("doGetAuthenticationInfo null token value");
            throw new AccountException("Token must be provided");
        }
        log.debug("doGetAuthenticationInfo for token {}", tokenValue);
        TokenRecord tokenRecord;
        TokenCredential tokenCredential;
        try {
            tokenRecord = database.findByTokenValue(tokenValue);
//            tokenCredential = database.findCredentialByTokenValue(tokenValue);
            if (tokenRecord == null) {
                log.debug("doGetAuthenticationInfo token value not found in database: {}", tokenValue);
                return null;
            }
            tokenCredential = tokenRecord.getCredential();
            if (tokenCredential == null || !tokenCredential.getValue().equals(tokenValue)) {
                log.debug("doGetAuthenticationInfo found record but token value does not match: {}", tokenValue);
                return null;
            }
            log.debug("doGetAuthenticationInfo found token {}", tokenCredential.getValue());
        } catch (Exception e) {
            log.debug("doGetAuthenticationInfo error", e);
            throw new AuthenticationException("Internal server error", e);
        }

        // automatically keep session alive if the token was created with the keepalive feature specified
        if( tokenCredential.getKeepalive() != null ) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(c.getTimeInMillis()+tokenCredential.getKeepalive());
            Date notAfter = c.getTime();
            TokenCredential updated = new TokenCredential(tokenCredential.getValue(), tokenCredential.getNotBefore(), notAfter, tokenCredential.getUsed(), tokenCredential.getUsedMax(), tokenCredential.getKeepalive());
            database.update(tokenCredential.getValue(), updated);
        }
                
        SimplePrincipalCollection principals = new SimplePrincipalCollection();
        principals.add(new Token(tokenCredential.getValue()), getName());
        principals.add(new Username(tokenRecord.getUsername()), getName());
        principals.add(new UsernameWithPermissions(tokenRecord.getUsername(), tokenRecord.getPermissions()), getName());
        log.debug("Added Username principal: {}", tokenRecord.getUsername());

        TokenAuthenticationInfo info = new TokenAuthenticationInfo();
        info.setPrincipals(principals);
        info.setCredentials(tokenCredential);

        return info;
    }

    public static class FileTokenDatabase {

        private final ObjectMapper mapper;
        private final String path;
        private HashMap<String, TokenRecord> map = new HashMap<>();

        public FileTokenDatabase() {
            path = Folders.repository("login-tokens");
            mapper = JacksonObjectMapperProvider.createDefaultMapper();
        }

        private File getTokenFile(String token) {
            return new File(path + File.separator + token);
        }

        private void write(File tokenFile, TokenRecord tokenRecord) {
            try {
                String json = mapper.writeValueAsString(tokenRecord);

                log.debug("Token: {}", json);

                if (!tokenFile.getParentFile().exists()) {
                    tokenFile.getParentFile().mkdirs();
                }

                try (FileOutputStream out = new FileOutputStream(tokenFile)) {
                    IOUtils.write(json, out, Charset.forName("UTF-8"));
                }

                // only store in memory if we were able to also store on disk
                map.put(tokenRecord.getCredential().getValue(), tokenRecord);
             } catch (IOException e) {
                log.error("Cannot store token in file: {}", tokenFile.getAbsolutePath(), e);
                throw new IllegalStateException("Cannot store token");
            }
       }
        
        public void add(TokenRecord tokenRecord) {
            TokenRecord existing = map.get(tokenRecord.getCredential().getValue());
            if (existing != null) {
                throw new IllegalArgumentException("Token already registered");
            }
            File tokenFile = getTokenFile(tokenRecord.getCredential().getValue());
            if (tokenFile.exists()) {
                throw new IllegalArgumentException("Token already registered");
            }

                write(tokenFile, tokenRecord);
                log.debug("Added token {}", tokenRecord.getCredential().getValue());
        }

        public void update(String value, TokenCredential update) {
            // to be eligible for update, a file token must actually be present on disk,
            // because the memory copy is just for faster reads.
            File tokenFile = getTokenFile(value);
            if (!tokenFile.exists()) {
                throw new IllegalArgumentException("Token not found");
            }
            
            TokenRecord tokenRecord;
            try(FileInputStream in = new FileInputStream(tokenFile)) {
                tokenRecord = mapper.readValue(in, TokenRecord.class);
            } catch (IOException e) {
                log.error("Cannot read token in file: {}", tokenFile.getAbsolutePath(), e);
                throw new IllegalStateException("Cannot update token");
            }
            
            assert tokenRecord != null;
            
                TokenCredential existing = tokenRecord.getCredential();
            
            // enforce rules... we don't allow lowering the uses but we do allow changing the max, and we allow changing the notAfter date
                if (existing.getUsed() != null && (update.getUsed() == null || update.getUsed() < existing.getUsed())) {
                    throw new IllegalArgumentException("Invalid 'used' count");
                }
                
                UsernameWithPermissions usernameWithPermissions = new UsernameWithPermissions(tokenRecord.getUsername(), tokenRecord.getPermissions());
                tokenRecord = new TokenRecord(update, usernameWithPermissions);
                
                // replace existing token
                write(tokenFile, tokenRecord);
                log.debug("Updated token {}", tokenRecord.getCredential().getValue());
        }

        public TokenRecord getByTokenValue(String value) {
            TokenRecord existing = findByTokenValue(value);
            if (existing == null) {
                throw new IllegalArgumentException("Token not found");
            }
            return existing;
        }

        public TokenRecord findByTokenValue(String value) {
            TokenRecord existing = map.get(value);
            if (existing != null) {
                return existing;
            }
            File tokenFile = getTokenFile(value);
            if (tokenFile.exists()) {
                try(FileInputStream in = new FileInputStream(tokenFile)) {
                    existing = mapper.readValue(in, TokenRecord.class);
                    map.put(value, existing);
                    return existing;
                } catch (IOException e) {
                    log.error("Cannot read token in file: {}", tokenFile.getAbsolutePath(), e);
                    throw new IllegalStateException("Cannot update token");
                }
            }
            return null;
        }

        public TokenCredential findCredentialByTokenValue(String value) {
            TokenRecord existing = findByTokenValue(value);
            if (existing == null) {
                return null;
            }
            return existing.getCredential();
        }

        public String findUsernameByTokenValue(String value) {
            TokenRecord existing = findByTokenValue(value);
            if (existing == null) {
                return null;
            }
            return existing.getUsername();
        }
        
        public Set<String> findPermissionsByTokenValue(String value) {
            TokenRecord existing = findByTokenValue(value);
            if (existing == null) {
                return null;
            }
            return existing.getPermissions();
        }
    }
}
