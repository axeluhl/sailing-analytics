package com.sap.sailing.server.impl;

import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;

/**
 * Bundle tracker for tracking of the lifecycle of important runtime bundles based on the extender pattern. In order to
 * track a bundle just add 'TrackLifecycle: true' to its OSGI manifest (MANIFEST.MF).
 * 
 * @author Frank (C5163874)
 * 
 */
public class ExtenderBundleTracker extends BundleTracker<Bundle> {

    private final Logger logger = Logger.getLogger(ExtenderBundleTracker.class.getName());

    private static int allBundleStatesMask = Bundle.UNINSTALLED | Bundle.INSTALLED | Bundle.RESOLVED | Bundle.STARTING | Bundle.STOPPING | Bundle.ACTIVE;
    
    public ExtenderBundleTracker(BundleContext context) {
        super(context, allBundleStatesMask, null);
    }

    @Override
    public Bundle addingBundle(Bundle bundle, BundleEvent event) {
        String symbolicName = bundle.getSymbolicName();
        logger.info(symbolicName + " " + getBundleStateAsText(event));

        return bundle;
    }

    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent event, Bundle object) {
        String symbolicName = bundle.getSymbolicName();
        logger.info(symbolicName + " " + getBundleStateAsText(event));
        if (event != null && event.getType() == BundleEvent.STOPPING) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            StringBuffer b = new StringBuffer();
            for (StackTraceElement traceElement : stackTrace) {
                b.append(traceElement.toString() + "\n");
            }
            logger.severe(b.toString());
        }
        String trackManifestEntry = (String) bundle.getHeaders().get("TrackLifecycle");
        if (trackManifestEntry != null) {
            trackLifecycle(bundle, event);
        }
    }

    private void trackLifecycle(Bundle bundle, BundleEvent event) {
        if(event != null) {
            switch(event.getType()) {
            case BundleEvent.STOPPING:
                logger.severe("Bundle " + bundle.getSymbolicName() + " STOPPING");
                logger.severe("--- " + event);
                break;
            case BundleEvent.STOPPED:
                logger.severe("Bundle " + bundle.getSymbolicName() + " STOPPED");
                logger.severe("--- " + event);
                break;
            }
        }
    }

    private String getBundleStateAsText(BundleEvent event) {
        String bundleState = "";
        if(event != null) {
            switch (event.getType()) {
                case BundleEvent.INSTALLED: bundleState = "INSTALLED";
                break;
                case BundleEvent.RESOLVED: bundleState = "RESOLVED";
                break;
                case BundleEvent.LAZY_ACTIVATION: bundleState = "LAZY_ACTIVATION";
                break;
                case BundleEvent.STARTING: bundleState = "STARTING";
                break;
                case BundleEvent.STARTED: bundleState = "STARTED";
                break;
                case BundleEvent.STOPPING: bundleState = "STOPPING";
                break;
                case BundleEvent.STOPPED: bundleState = "STOPPED";
                break;
                case BundleEvent.UPDATED: bundleState = "UPDATED";
                break;
                case BundleEvent.UNRESOLVED: bundleState = "UNRESOLVED";
                break;
                case BundleEvent.UNINSTALLED: bundleState = "UNINSTALLED";
                break;
            }
        }
        return bundleState;
    }
}
