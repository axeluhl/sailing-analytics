package com.sap.sailing.server.replication.impl;

import java.io.IOException;
import java.util.UUID;
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
    
    public static final String REPLICATE_ON_START = "REPLICATE_ON_START";
    public static final String REPLICATE_MASTER_SERVLET_HOST = "REPLICATE_MASTER_SERVLET_HOST";
    public static final String REPLICATE_MASTER_SERVLET_PORT = "REPLICATE_MASTER_SERVLET_PORT";
    public static final String REPLICATE_MASTER_QUEUE_HOST = "REPLICATE_MASTER_QUEUE_HOST";
    public static final String REPLICATE_MASTER_QUEUE_PORT = "REPLICATE_MASTER_QUEUE_PORT";

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
                exchangeHost = System.getenv(REPLICATION_HOST);
            }
        }
        replicationInstancesManager = new ReplicationInstancesManager();
        ReplicationService serverReplicationMasterService = new ReplicationServiceImpl(exchangeName, exchangeHost, replicationInstancesManager);
        bundleContext.registerService(ReplicationService.class, serverReplicationMasterService, null);
        logger.info("Registered replication service "+serverReplicationMasterService+" using exchange name "+exchangeName+" on host "+exchangeHost);
        checkIfAutomaticReplicationShouldStart(serverReplicationMasterService);
    }
    
    private void checkIfAutomaticReplicationShouldStart(ReplicationService serverReplicationMasterService) {
        if (System.getenv(REPLICATE_ON_START).equals("True")) {
            logger.info("Configuration requested automatic replication. Starting up...");
            ReplicationMasterDescriptorImpl master = new ReplicationMasterDescriptorImpl(
                    System.getenv(REPLICATE_MASTER_QUEUE_HOST),
                    System.getenv(REPLICATE_MASTER_SERVLET_HOST), 
                    System.getenv(REPLICATION_CHANNEL), 
                    Integer.valueOf(System.getenv(REPLICATE_MASTER_SERVLET_PORT).trim()), 
                    Integer.valueOf(System.getenv(REPLICATE_MASTER_QUEUE_PORT).trim()), 
                    serverReplicationMasterService.getServerIdentifier().toString());
            try {
                serverReplicationMasterService.startToReplicateFrom(master);
                logger.info("Automatic replication has been started.");
            } catch (ClassNotFoundException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }
    
    public static BundleContext getDefaultContext() {
        return defaultContext;
    }
}