package com.sap.sailing.monitoring;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Activator for the monitoring service
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Nov 25, 2012
 */
public class Activator implements BundleActivator {
    private static BundleContext context;
    private AbstractPortMonitor monitor;

    static BundleContext getContext() {
        return context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;

        /* Load properties */
        File propertiesfile = new File(new File(System.getProperty("jetty.home")).getParent()
                + "/monitoring.properties");
        Properties props = new Properties();
        props.load(new FileReader(propertiesfile));

        /* Starts a new port monitoring app */
        monitor = new OSGiRestartingPortMonitor(props);
        monitor.startMonitoring();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        monitor.stopMonitoring();
        Activator.context = null;
    }

}
