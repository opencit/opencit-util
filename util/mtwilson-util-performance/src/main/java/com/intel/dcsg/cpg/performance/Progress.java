/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance;

/**
 *
 * NOTE: "current" means the number of steps currently COMPLETED, so when
 * getCurrent() == getMax() the job must be done already, not working on the
 * last step. Conversely, when getCurrent() == 0 the job may be pending or it
 * may already be working on (but not yet completed) the first step.
 *
 * TODO:  getCurrent() should be renamed to getProgress() and getMax() should be
 * renamed to getProgressMax()
 * 
 * For progress reporting, "progress" means successfully completed steps.
 * 
 * @author jbuhacoff
 */
public interface Progress {

    /**
     *
     * Examples of current/max: 0/5 (pending), 1/5, 2/5, ... (in progress), 5/5
     * (done)
     *
     * The current value should be always increasing - the only time it should
     * decrease is when reset to zero prior to starting over. A process that has
     * to "go back" to a prior step should model that as increasing the max
     * number of steps. For example to "go back" from step 5 to step 3 of an 9
     * step process, add two steps to max so that 5/9 becomes 5/11 to show there
     * are two more steps to do besides the original 4 remaining steps.
     *
     * @return current number of completed steps; zero if processing hasn't
     * started or before the first step is complete
     */
    long getCurrent();

    /**
     * Whenever possible, it should be calculated in advance of starting the
     * work. It's ok for the max number to increase while processing as more
     * steps are discovered, typically based on runtime conditions.
     *
     *
     * @return the maximum number of steps that can be performed
     */
    long getMax();
}
