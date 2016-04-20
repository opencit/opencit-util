/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.util;

/**
 * Classes that implement Copyable are able to copy attributes
 * from other instances to themselves.  It works the opposite
 * way of Cloneable. 
 * 
 * Example:
 * 
 * Object from = new Object();
 * Object to = new Object();
 * to.copy(from);
 * // now "to" is equal to "from"
 * 
 * Standard pattern for the implementation of the copy method:
 * 
 * public void copy(Object other) {
 *   super.copy(other); // only if superclass is also Copyable
 *   if( other instanceof ThisClassOrKnownClass ) {
 *      this.field1 = other.getField1();
 *      this.field2 = other.getField2();
 *      // etc
 *   }
 * }
 * 
 * @author jbuhacoff
 */
public interface Copyable {
    void copy(Object other);
}
