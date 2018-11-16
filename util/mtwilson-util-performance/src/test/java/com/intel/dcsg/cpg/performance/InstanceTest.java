/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance;

import com.intel.dcsg.cpg.performance.report.PerformanceInfo;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class InstanceTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InstanceTest.class);
    
    /**
     * HP EliteBook 8560w, Intel Core i5-2540M CPU 2.60GHz, 16GB RAM
     * 
     * getClass().isInstance(Object.class):
     * avg 0.002ms/iteration for 1000 iterations
     * avg 0.00009ms/iteration for 100,000 iterations
     * avg 0.0000177ms/iteration for 20,000,000 iterations
     * 
     * !(this instanceof Object)
     * avg 0.003ms/iteration for 1000 iterations
     * avg 0.00003ms/iteration for 100,000 iterations
     * avg 0.00001745ms/iteration for 20,000,000 iterations
     */
    @Test
    public void testInstanceCheckPerformance() {
        int n = 100;
        log.debug("Checking for {} iterations", n);
        long totalstart = System.currentTimeMillis();        
        long elapsedTimes[] = new long[n];
        for(int i=0; i<n; i++) {
            long start = System.currentTimeMillis();
            //if( getClass().isInstance(Object.class)) { log.debug("never see this because Object is NOT an instance of InstanceTest"); }
            if( !(this instanceof Object) ) { log.debug("never see this because this is an instance of Object"); }
            long end = System.currentTimeMillis();
            elapsedTimes[i] = end - start;
        }
        long totalend = System.currentTimeMillis();        
        PerformanceInfo info = new PerformanceInfo(elapsedTimes);
        printPerformanceInfo(info);
        log.debug("Total time elapse: {}ms", totalend-totalstart);
    }
    private static void printPerformanceInfo(PerformanceInfo info) {
        log.debug("Number of executions: {}", info.getData().length);
        log.debug("Average time: {} ms", info.getAverage());
        log.debug("Min time: {} ms", info.getMin());
        log.debug("Max time: {} ms", info.getMax());
    }
    
    
}
