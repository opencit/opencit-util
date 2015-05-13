/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.jdbi;

//import com.intel.mtwilson.My;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.launcher.ext.annotations.Background;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 *
 * @author jbuhacoff
 */
@Background
public class ArchiveRequestLog implements Runnable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ArchiveRequestLog.class);

    @Override
    public void run() {
        try (LoginDAO dao = MyJdbi.authz()) {
            // get the configured window size (in time) 
            Configuration configuration = ConfigurationFactory.getConfiguration();
            int expiresAfter = Integer.valueOf(configuration.get("mtwilson.security.x509.request.expires", "3600000")); // milliseconds, default 1 hr //My.configuration().getAntiReplayProtectionWindowMilliseconds(); 
            Calendar expirationTime = Calendar.getInstance();
            expirationTime.add(Calendar.MILLISECOND, -expiresAfter);
            // delete requests older than the expiration time
            dao.deleteRequestLogEntriesEarlierThan(expirationTime.getTime());
        } catch (IOException | SQLException e) {
            log.error("Error while archiving old requests", e);
        }
    }
    
    
}
