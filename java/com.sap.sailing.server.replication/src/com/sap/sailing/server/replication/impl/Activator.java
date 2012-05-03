package com.sap.sailing.server.replication.impl;

import java.io.File;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.server.replication.ReplicationService;

public class Activator implements BundleActivator {
    private static final String REPLICATION_PERSISTENCE_DIR_PROPERTY = "replication.persistenceDir";

    private static final String BROKER_URL_PROPERTY = "replication.brokerURL";

    private static final String REPLICATION_USE_JMX_PROPERTY = "replication.useJMX";

    private MessageBrokerManager messageBrokerManager;

    private ReplicationInstancesManager replicationInstancesManager;
    
    private static BundleContext defaultContext;

    public void start(BundleContext bundleContext) throws Exception {
        defaultContext = bundleContext;
        String replicationPersistenceDirectory = bundleContext.getProperty(REPLICATION_PERSISTENCE_DIR_PROPERTY);
        if (replicationPersistenceDirectory == null) {
            replicationPersistenceDirectory = System.getProperty("java.io.tmpdir");
        }
        String brokerURL = bundleContext.getProperty(BROKER_URL_PROPERTY);
        if (brokerURL == null) {
            brokerURL = "tcp://localhost:61616";
        }
        MessageBrokerConfiguration brokerConfig = new MessageBrokerConfiguration("SailingServerReplicationBroker",
                brokerURL, new File(replicationPersistenceDirectory, "kahadb").getAbsolutePath());
        messageBrokerManager = new MessageBrokerManager(brokerConfig);
        String useJMX = bundleContext.getProperty(REPLICATION_USE_JMX_PROPERTY);
        if (useJMX == null || useJMX.length() == 0) {
            useJMX = "false";
        }
        messageBrokerManager.startMessageBroker(Boolean.valueOf(useJMX));
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