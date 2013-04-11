package com.sap.sailing.server.replication.impl;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.server.replication.ReplicationService;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    private static final String PROPERTY_NAME_EXCHANGE_NAME = "replication.exchangeName";

    private ReplicationInstancesManager replicationInstancesManager;
    
    private static BundleContext defaultContext;
    
    public void start(BundleContext bundleContext) throws Exception {
        defaultContext = bundleContext;
        String exchangeName = bundleContext.getProperty(PROPERTY_NAME_EXCHANGE_NAME);
        if (exchangeName == null) {
            exchangeName = "sapsailinganalytics";
        }
        replicationInstancesManager = new ReplicationInstancesManager();
        ReplicationService serverReplicationMasterService = new ReplicationServiceImpl(exchangeName, replicationInstancesManager);
        bundleContext.registerService(ReplicationService.class, serverReplicationMasterService, null);
        logger.info("Registered replication service "+serverReplicationMasterService+" using exchange name "+exchangeName);
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }
    
    public static BundleContext getDefaultContext() {
        return defaultContext;
    }
}