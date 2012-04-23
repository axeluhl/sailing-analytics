package com.sap.sailing.server.replication.impl;

import java.io.File;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.server.replication.ReplicationService;

public class Activator implements BundleActivator {
    private MessageBrokerManager messageBrokerManager;

    private ReplicationInstancesManager replicationInstancesManager;

    public void start(BundleContext bundleContext) throws Exception {
        MessageBrokerConfiguration brokerConfig = new MessageBrokerConfiguration();
        brokerConfig.setBrokerUrl("tcp://localhost:61616");
        String tmpDir = System.getProperty("java.io.tmpdir");
        brokerConfig.setDataStoreDirectory(new File(tmpDir, "kahadb").getAbsolutePath());
        brokerConfig.setBrokerName("SailingServerReplicationBroker");
        messageBrokerManager = new MessageBrokerManager(brokerConfig);
        messageBrokerManager.startMessageBroker();
        messageBrokerManager.createAndStartConnection();
        replicationInstancesManager = new ReplicationInstancesManager();
        ReplicationService serverReplicationMasterService = new ReplicationServiceImpl(replicationInstancesManager, messageBrokerManager, true);
        bundleContext.registerService(ReplicationService.class, serverReplicationMasterService, null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        messageBrokerManager.closeSessions();
        messageBrokerManager.closeConnections();
        messageBrokerManager.stopMessageBroker();
    }
}