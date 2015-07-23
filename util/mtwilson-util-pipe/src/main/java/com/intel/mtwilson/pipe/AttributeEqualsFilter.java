/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.pipe;

import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.PropertyUtils;

/**
 * Currently using commons-beanutils but could use reflection directly like
 * the ValidationUtil and the PatchUtil classes. 
 *
 * This filter must be initialized with an attribute name (String) and
 * an attribute value (Object). It can then evaluate candidates by
 * checking if they have a non-null attribute (JavaBean property) equal to that
 * value.
 *
 * @param <T>
 */
public class AttributeEqualsFilter<T> implements Filter<T> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AttributeEqualsFilter.class);

    private String attributeName;
    private Object attributeValue;

    public AttributeEqualsFilter(String attributeName, Object attributeValue) {
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    @Override
    public boolean accept(T item) {
        try {
            Object targetValue = PropertyUtils.getProperty(item, attributeName); // getProperty supports nested properties like "class.name" which resolves to getClass().getName()
            log.debug("Item {} attribute {} value {}", item.getClass().getName(), attributeName, targetValue);
            return targetValue != null && targetValue.equals(attributeValue);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.warn("Cannot evaluate item {} attribute {} due to exception: {}", item.getClass().getName(), attributeName, e.toString());
            return false;
        }
    }
    
}
