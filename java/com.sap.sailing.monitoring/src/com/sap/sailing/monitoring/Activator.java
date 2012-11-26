package com.sap.sailing.monitoring;

import java.net.InetAddress;
import java.net.InetSocketAddress;

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
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;

		/* Configuration */
		InetSocketAddress[] endpoints = new InetSocketAddress[]{
		        new InetSocketAddress(InetAddress.getByName("localhost"), 8080)
		};
		
		String[] services = new String[] {
		        "com.sap.sailing.server"
		};
		
		/* Starts a new port monitoring app */
		monitor = new OSGiRestartingPortMonitor(endpoints, services, 10000);
		monitor.startMonitoring();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
	    monitor.stopMonitoring();
		Activator.context = null;
	}

}
