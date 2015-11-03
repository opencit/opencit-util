/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.core.data.bundle;

import java.util.Iterator;

/**
 * Represents a feature that contributes files to the bundle for export.
 * 
 * @author jbuhacoff
 */
public interface Contributor {
    Iterator<Entry> contribute();
    void receive(Bundle archive);
}
