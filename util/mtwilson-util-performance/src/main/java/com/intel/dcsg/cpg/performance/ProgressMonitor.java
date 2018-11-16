/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitors progress of a task using a separate thread, sending periodic updates
 * to an observer.
 *
 * The assumption is that the task has a fixed number of steps and provides its
 * status by indicating how many steps it has completed.
 *
 * @author jbuhacoff
 */
public class ProgressMonitor {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProgressMonitor.class);
    private Monitor<Progress> monitor;
    private Progress task;
    private Observer<Progress> observer;
    private ProgressValue report;

    public ProgressMonitor(Progress task) {
        this.task = task;
        this.observer = new ProgressLogObserver();
    }

    public ProgressMonitor(Progress task, Observer<Progress> observer) {
        this.task = task;
        this.observer = observer;
    }

    /**
     * The status starts with current=0,max=1 so that callers don't need
     * to check for divide-by-zero.  It doesn't make sense for any progress
     * reporting application to use max=0 as it's not possible to report any
     * progress against that.
     */
    public static class ProgressValue implements Value<Progress> {
        private Progress progress;
        private long current = 0;
        private long max = 1;

        public ProgressValue(Progress progress) {
            this.progress = progress;
        }

        @Override
        public Progress getValue() {
            current = progress.getCurrent();
            max = progress.getMax();
            return progress;
        }

        /**
         * The last reported current value is updated every time getValue() is
         * called. To get the actual current value, call getValue().getCurrent()
         * and it will update this last reported current value. 
         * Use this last reported current value to observe the last reported
         * value without updating it.
         * 
         * @return the last reported "current" value, not the actual "current" value
         */
        public long getLastReportedCurrent() {
            return current;
        }

        /**
         * The last reported max value is updated every time getValue() is
         * called. To get the actual max value, call getValue().getMax()
         * and it will update this last reported max value. 
         * Use this last reported max value to observe the last reported
         * value without updating it.
         * 
         * @return the last reported "max" value, not the actual "max" value
         */
        public long getLastReportedMax() {
            return max;
        }
        
        
    }

    public static class ProgressLogObserver implements Observer<Progress> {

        private Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public void observe(Progress object) {
            log.debug("Progress: {} of {}", object.getCurrent(), object.getMax());
        }
    }

    public void start() {
        report = new ProgressValue(task);
        monitor = new Monitor<>(report, observer);
        monitor.start();
    }

    public void stop() {
        if (monitor == null) {
            throw new IllegalStateException("Monitor is not started");
        }
        monitor.stop();
        // because this is a progress monitor, we want to ensure the last
        // update captured the final progress 
        // for example:
        // ProgressMonitor stopping with actual current: 5  max: 5 compared to last reported current: 4 max: 5
        log.debug("ProgressMonitor stopping with actual current: {}  max: {} compared to last reported current: {} max: {}", task.getCurrent(), task.getMax(), report.getLastReportedCurrent(), report.getLastReportedMax());
        if ( report.getLastReportedCurrent() < task.getCurrent() || report.getLastReportedMax() < task.getMax() ) {
            monitor.monitor();
        }
    }
}
