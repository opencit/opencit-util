/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.pipe;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class AttributeEqualsFilterTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AttributeEqualsFilterTest.class);

    public static class Foo {
        private String bar = "baz";

        public String getBar() {
            return bar;
        }
        
    }
    
    @Test
    public void testClassAttribute() {
        AttributeEqualsFilter filter = new AttributeEqualsFilter("class.name", "com.intel.mtwilson.pipe.AttributeEqualsFilterTest$Foo");
        assertTrue(filter.accept(new Foo()));
    }
}
