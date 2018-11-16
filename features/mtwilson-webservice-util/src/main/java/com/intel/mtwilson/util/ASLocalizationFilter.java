/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util;

//import com.intel.mtwilson.My;
import com.intel.dcsg.cpg.i18n.LocalizableResponseFilter;
import com.intel.mtwilson.core.i18n.LocalizationUtil;
import java.io.IOException;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;

/**
 *
 * @author jbuhacoff
 */
@Priority(Priorities.ENTITY_CODER)
public class ASLocalizationFilter extends LocalizableResponseFilter {
    public ASLocalizationFilter() throws IOException {
        setAvailableLocales(LocalizationUtil.getAvailableLocales());
//        setAvailableLocales(My.configuration().getAvailableLocales());
    }
}
