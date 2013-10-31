package com.sap.sailing.server.replication.impl;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.server.replication.ReplicationService;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    private static final String PROPERTY_NAME_EXCHANGE_NAME = "replication.exchangeName";
    private static final String PROPERTY_NAME_EXCHANGE_HOST = "replication.exchangeHost";
    
    public static final String REPLICATION_CHANNEL = "REPLICATION_CHANNEL";
    public static final String REPLICATION_HOST = "REPLICATION_HOST";

    private ReplicationInstancesManager replicationInstancesManager;
    
    private static BundleContext defaultContext;
    
    public void start(BundleContext bundleContext) throws Exception {
        defaultContext = bundleContext;
        String exchangeName = bundleContext.getProperty(PROPERTY_NAME_EXCHANGE_NAME);
        String exchangeHost = bundleContext.getProperty(PROPERTY_NAME_EXCHANGE_HOST);
        if (exchangeName == null) {
            if (System.getenv(REPLICATION_CHANNEL) == null) {
                exchangeName = "sapsailinganalytics";
            } else {
                exchangeName = System.getenv(REPLICATION_CHANNEL);
            }
        }
        if (exchangeHost == null) {
            if (System.getenv(REPLICATION_HOST) == null) {
                exchangeHost = "localhost";
            } else {
                logger.info("Using environment variable REPLICATION_HOST as host!");
                exchangeHost = System.getenv(REPLICATION_HOST);
            }
        }
        replicationInstancesManager = new ReplicationInstancesManager();
        ReplicationService serverReplicationMasterService = new ReplicationServiceImpl(exchangeName, exchangeHost, replicationInstancesManager);
        bundleContext.registerService(ReplicationService.class, serverReplicationMasterService, null);
        logger.info("Registered replication service "+serverReplicationMasterService+" using exchange name "+exchangeName);
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }
    
    public static BundleContext getDefaultContext() {
        return defaultContext;
    }
}