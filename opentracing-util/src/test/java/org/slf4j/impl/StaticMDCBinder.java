package org.slf4j.impl;

import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;

/**
 * This is so we can test MDC functionality without having to pull in logback/log4j/log4j2
 * We cannot use slf4j-simple because it uses a no-op implementation of MDC
 */
public class StaticMDCBinder {
    public static final StaticMDCBinder SINGLETON = new StaticMDCBinder();

    private StaticMDCBinder() {
    }

    public static final StaticMDCBinder getSingleton() {
        return SINGLETON;
    }

    public MDCAdapter getMDCA() {
        return new BasicMDCAdapter();
    }

    public String getMDCAdapterClassStr() {
        return BasicMDCAdapter.class.getName();
    }
}
