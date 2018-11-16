/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.core.i18n;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.i18n.LocaleUtil;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import java.io.IOException;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class LocalizationUtilTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LocalizationUtilTest.class);

    @Test
    public void testRetrieveLocale() throws IOException {
        String[] availableLocales = LocalizationUtil.getAvailableLocales();
        log.debug("Locales: {}", (String[]) availableLocales);
    }
}
