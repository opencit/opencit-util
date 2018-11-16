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

/**
 *
 * @author jbuhacoff
 */
public class LocalizationUtil {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LocalizationUtil.class);

    /**
     * Returns the list of locales configured in "mtwilson.locales" , or the
     * platform default locale if there is nothing configured.
     *
     * @return an array with at least one locale
     */
    public static String[] getAvailableLocales() {
        try {
            Configuration conf = ConfigurationFactory.getConfiguration();
            // example property in file:  mtwilson.locales=en,en-US,es,es-MX
            // the getString(key) function will return the text only up to the first comma, e.g. "en"
            // the getStringArray(key) function never returns null,  if the key is missing or null it returns empty array, and if the value is empty string it returns an array with one element whose value is empty string
            String localesCSV = conf.get("mtwilson.locales", "");
//        String[] locales = conf.getStringArray("mtwilson.locales");
            String[] locales = StringUtils.split(localesCSV, ", ");
            if (locales == null || locales.length == 0 || locales[0] == null || locales[0].isEmpty()) {
                return new String[]{LocaleUtil.toLanguageTag(Locale.getDefault())};
            }
            return locales;
        } catch (IOException e) {
            log.error("Cannot load configuration", e);
            return new String[]{LocaleUtil.toLanguageTag(Locale.getDefault())};
        }
    }
}
