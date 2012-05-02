package com.sap.sailing.server.replication.impl;

import java.io.File;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.server.replication.ReplicationService;

public class Activator implements BundleActivator {
    private MessageBrokerManager messageBrokerManager;

    private ReplicationInstancesManager replicationInstancesManager;
    
    private static BundleContext defaultContext;

    public void start(BundleContext bundleContext) throws Exception {
        defaultContext = bundleContext;
        String tmpDir = System.getProperty("java.io.tmpdir");
        MessageBrokerConfiguration brokerConfig = new MessageBrokerConfiguration("SailingServerReplicationBroker",
                "tcp://localhost:61616", new File(tmpDir, "kahadb").getAbsolutePath());
        messageBrokerManager = new MessageBrokerManager(brokerConfig);
        messageBrokerManager.startMessageBroker(true);
        messageBrokerManager.createAndStartConnection();
        replicationInstancesManager = new ReplicationInstancesManager();
        ReplicationService serverReplicationMasterService = new ReplicationServiceImpl(replicationInstancesManager, messageBrokerManager);
        bundleContext.registerService(ReplicationService.class, serverReplicationMasterService, null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        messageBrokerManager.closeSessions();
        messageBrokerManager.closeConnections();
        messageBrokerManager.stopMessageBroker();
    }
    
    public static BundleContext getDefaultContext() {
        return defaultContext;
    }
}