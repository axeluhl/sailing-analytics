package com.sap.sailing.server.replication.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.server.replication.ServerReplicationMasterService;
import com.sap.sailing.server.replication.ServerReplicationSlaveService;

public class Activator implements BundleActivator {
    private MessageBrokerManager messageBrokerManager;

    private ReplicationInstancesManager replicationInstancesManager;

    public void start(BundleContext bundleContext) throws Exception {
        MessageBrokerConfiguration brokerConfig = new MessageBrokerConfiguration();
        brokerConfig.setBrokerUrl("tcp://localhost:61616");
        brokerConfig.setDataStoreDirectory("c:\\temp\\kahadb");
        brokerConfig.setBrokerName("SailingServerReplicationBroker");
        
        messageBrokerManager = new MessageBrokerManager(brokerConfig);
        
        messageBrokerManager.startMessageBroker();
        messageBrokerManager.createAndStartConnection();
        
        replicationInstancesManager = new ReplicationInstancesManager();
        
        ServerReplicationSlaveService serverReplicationSlaveService = new ServerReplicationSlaveServiceImpl(replicationInstancesManager);  
        ServerReplicationMasterService serverReplicationMasterService = new ServerReplicationMasterServiceImpl(replicationInstancesManager, messageBrokerManager);
        
        bundleContext.registerService(ServerReplicationSlaveService.class, serverReplicationSlaveService, null);
        bundleContext.registerService(ServerReplicationMasterService.class, serverReplicationMasterService, null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        messageBrokerManager.closeSessions();
        messageBrokerManager.closeConnections();
        messageBrokerManager.stopMessageBroker();
    }
}