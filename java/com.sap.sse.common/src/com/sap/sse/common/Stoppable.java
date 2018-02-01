package com.sap.sse.common;

/**
 * Common definition for classes that hold resources that must be explicitly cleaned up.
 * 
 * This can be used to e.g. recognize all {@link Stoppable} instance in a {@link org.osgi.framework.BundleActivator}.
 * This makes it possible to cleanup all {@link Stoppable} instances in a loop to make the code much shorter.
 */
public interface Stoppable {
    void stop();
}
