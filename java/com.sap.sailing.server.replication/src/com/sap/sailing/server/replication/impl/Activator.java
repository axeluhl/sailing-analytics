package com.sap.sailing.server.replication.impl;

import java.io.IOException;
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
    
    public static final String REPLICATE_ON_START = "replicate.on.start";
    public static final String REPLICATE_MASTER_SERVLET_HOST = "replicate.master.servlet.host";
    public static final String REPLICATE_MASTER_SERVLET_PORT = "replicate.master.servlet.port";
    public static final String REPLICATE_MASTER_QUEUE_HOST = "replicate.master.queue.host";
    public static final String REPLICATE_MASTER_QUEUE_PORT = "replicate.master.queue.port";
    public static final String REPLICATE_MASTER_QUEUE_NAME = "replicate.master.queue.name";

    private ReplicationInstancesManager replicationInstancesManager;
    
    private static BundleContext defaultContext;
    
    public void start(BundleContext bundleContext) throws Exception {
        defaultContext = bundleContext;
        String exchangeName = bundleContext.getProperty(PROPERTY_NAME_EXCHANGE_NAME);
        String exchangeHost = bundleContext.getProperty(PROPERTY_NAME_EXCHANGE_HOST);
        String queueName 	= System.getProperty(REPLICATE_MASTER_QUEUE_NAME);
        if (exchangeName == null) {
            if (System.getenv(REPLICATION_CHANNEL) == null && queueName == null) {
                exchangeName = "sapsailinganalytics";
            } else if (queueName != null ){
            	exchangeName = queueName; 
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
        checkIfAutomaticReplicationShouldStart(serverReplicationMasterService, exchangeName);
    }
    
    private void checkIfAutomaticReplicationShouldStart(ReplicationService serverReplicationMasterService, String exchangeName) {
        String replicateOnStart = System.getProperty(REPLICATE_ON_START);
        if (replicateOnStart != null && replicateOnStart.equals("True")) {
            logger.info("Configuration requested automatic replication. Starting up...");
            ReplicationMasterDescriptorImpl master = new ReplicationMasterDescriptorImpl(
                    System.getProperty(REPLICATE_MASTER_QUEUE_HOST),
                    System.getProperty(REPLICATE_MASTER_SERVLET_HOST), 
                    exchangeName, 
                    Integer.valueOf(System.getProperty(REPLICATE_MASTER_SERVLET_PORT).trim()), 
                    Integer.valueOf(System.getProperty(REPLICATE_MASTER_QUEUE_PORT).trim()), 
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