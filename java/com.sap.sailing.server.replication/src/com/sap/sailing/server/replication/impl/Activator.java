package com.sap.sailing.server.replication.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.server.replication.ReplicationService;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
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
        final File brokerPersistenceDir = new File(replicationPersistenceDirectory, "kahadb");
        removeTemporaryTestBrokerPersistenceDirectory(brokerPersistenceDir);
        MessageBrokerConfiguration brokerConfig = new MessageBrokerConfiguration("SailingServerReplicationBroker",
                brokerURL, brokerPersistenceDir.getAbsolutePath());
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

    public static void removeTemporaryTestBrokerPersistenceDirectory(File brokerPersistenceDir) throws FileNotFoundException {
        if (brokerPersistenceDir.exists() && brokerPersistenceDir.isDirectory()) {
            logger.info("Deleting message broker persistence director "+brokerPersistenceDir);
            deleteRecursive(brokerPersistenceDir);
        }
        File failoverStore = new File("activemq-data");
        if (failoverStore.exists() && failoverStore.isDirectory()) {
            logger.info("Deleting message broker failover store "+failoverStore);
            deleteRecursive(failoverStore);
        }
    }
    
    public static boolean deleteRecursive(File path) throws FileNotFoundException{
        if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
        boolean ret = true;
        if (path.isDirectory()){
            for (File f : path.listFiles()){
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
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