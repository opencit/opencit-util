/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.login.token;

import java.util.Date;

/**
 * The limits associated with a login token.
 * 
 * @author jbuhacoff
 */
public class LoginTokenAttributes {
    private Date notBefore;
    private Date notAfter;
    private Integer notMoreThan;

    public LoginTokenAttributes() {
    }

    public LoginTokenAttributes(Date notBefore, Date notAfter, Integer notMoreThan) {
        this.notBefore = notBefore;
        this.notAfter = notAfter;
        this.notMoreThan = notMoreThan;
    }

    /**
     * 
     * @return latest date on which the token is valid
     */
    public Date getNotAfter() {
        return notAfter;
    }

    /**
     * @return earliest date on which the token is valid
     */
    public Date getNotBefore() {
        return notBefore;
    }

    /**
     * 
     * @return the maximum times a token may be used
     */
    public Integer getNotMoreThan() {
        return notMoreThan;
    }

    public void setNotAfter(Date notAfter) {
        this.notAfter = notAfter;
    }

    public void setNotBefore(Date notBefore) {
        this.notBefore = notBefore;
    }

    public void setNotMoreThan(Integer notMoreThan) {
        this.notMoreThan = notMoreThan;
    }
    
    
}
