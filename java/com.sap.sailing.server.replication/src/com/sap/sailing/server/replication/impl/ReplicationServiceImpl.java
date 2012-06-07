package com.sap.sailing.server.replication.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.server.OperationExecutionListener;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.replication.ReplicaDescriptor;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;
import com.sap.sailing.server.replication.ReplicationService;

/**
 * Can observe a {@link RacingEventService} for the operations it performs that require replication. Only observes as
 * long as there are replicas registered. If the last replica is de-registered, the service stops observing the
 * {@link RacingEventService}. Operations received that require replication are broadcast to the
 * {@link #getReplicationTopic() replication topic}.
 * <p>
 * 
 * This service object {@link RacingEventService#addOperationExecutionListener(OperationExecutionListener) registers} as
 * listener at the {@link RacingEventService} so it {@link #executed(RacingEventServiceOperation) receives}
 * notifications about operations executed by the {@link RacingEventService} that require replication if and only if
 * there is at least one replica registered.
 * 
 * @author Frank Mittag, Axel Uhl (d043530)
 * 
 */
public class ReplicationServiceImpl implements ReplicationService, OperationExecutionListener, HasRacingEventService {
    private final ReplicationInstancesManager replicationInstancesManager;
    
    private final MessageBrokerManager messageBrokerManager;
    
    private MessageProducer messageProducer;
    
    private Topic replicationTopic;
    
    private ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    
    private final RacingEventService localService;
    
    /**
     * <code>null</code>, if this instance is not currently replicating from some master; the master's descriptor otherwise
     */
    private ReplicationMasterDescriptor replicatingFromMaster;
    
    /**
     * The UUIDs with which this replica is registered by the master identified by the corresponding key
     */
    private final Map<ReplicationMasterDescriptor, String> replicaUUIDs;
    
    public ReplicationServiceImpl(final ReplicationInstancesManager replicationInstancesManager,
            final MessageBrokerManager messageBrokerManager) throws Exception {
        this.replicationInstancesManager = replicationInstancesManager;
        replicaUUIDs = new HashMap<ReplicationMasterDescriptor, String>();
        this.messageBrokerManager = messageBrokerManager;
        racingEventServiceTracker = new ServiceTracker<RacingEventService, RacingEventService>(
                Activator.getDefaultContext(), RacingEventService.class.getName(), null);
        racingEventServiceTracker.open();
        localService = null;
    }
    
    /**
     * Like {@link #ReplicationServiceImpl(ReplicationInstancesManager, MessageBrokerManager)}, only that instead of using
     * an OSGi service tracker to discover the {@link RacingEventService}, the service to replicate is "injected" here.
     */
    public ReplicationServiceImpl(final ReplicationInstancesManager replicationInstancesManager,
            final MessageBrokerManager messageBrokerManager, RacingEventService localService) {
        this.replicationInstancesManager = replicationInstancesManager;
        replicaUUIDs = new HashMap<ReplicationMasterDescriptor, String>();
        this.messageBrokerManager = messageBrokerManager;
        this.localService = localService;
    }
    
    @Override
    public RacingEventService getRacingEventService() {
        RacingEventService result;
        if (localService != null) {
            result = localService;
        } else {
            result = racingEventServiceTracker.getService();
        }
        return result;
    }

    @Override
    public void registerReplica(ReplicaDescriptor replica) throws JMSException {
        Topic topic = getReplicationTopic();
        assert topic != null;
        if (!replicationInstancesManager.hasReplicas()) {
            addAsListenerToRacingEventService();
            messageBrokerManager.createAndStartConnection();
            messageBrokerManager.createSession(/* transacted */ false);
        }
        replicationInstancesManager.registerReplica(replica);
    }
    
    private void addAsListenerToRacingEventService() {
        getRacingEventService().addOperationExecutionListener(this);
    }

    @Override
    public void unregisterReplica(ReplicaDescriptor replica) throws JMSException {
        replicationInstancesManager.unregisterReplica(replica);
        if (!replicationInstancesManager.hasReplicas()) {
            removeAsListenerFromRacingEventService();
            messageBrokerManager.closeSessions();
            messageBrokerManager.closeConnections();
        }
    }

    private void removeAsListenerFromRacingEventService() {
        getRacingEventService().removeOperationExecutionListener(this);
        messageProducer = null;
    }

    private Topic getReplicationTopic() throws JMSException{
        if (replicationTopic == null) {
            Session session = messageBrokerManager.getSession();
            if (session == null) {
                session = messageBrokerManager.createSession(true);
            }
            replicationTopic = session.createTopic(SAILING_SERVER_REPLICATION_TOPIC);
        }
        return replicationTopic;
    }
    
    private void broadcastOperation(RacingEventServiceOperation<?> operation) throws Exception {
        Topic topic = getReplicationTopic();
        Session session = messageBrokerManager.getSession();
        getMessageProducer(topic).setDeliveryMode(DeliveryMode.PERSISTENT);
        BytesMessage operationAsMessage = session.createBytesMessage();
        // serialize operation into message
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(operation);
        oos.close();
        operationAsMessage.writeBytes(bos.toByteArray());
        messageProducer.send(operationAsMessage);
        replicationInstancesManager.log(operation);
    }

    private MessageProducer getMessageProducer(Topic topic) throws JMSException {
        if (messageProducer == null) {
            messageProducer = messageBrokerManager.getSession().createProducer(topic);
        }
        return messageProducer;
    }

    @Override
    public Iterable<ReplicaDescriptor> getReplicaInfo() {
        return replicationInstancesManager.getReplicaDescriptors();
    }

    @Override
    public ReplicationMasterDescriptor isReplicatingFromMaster() {
        return replicatingFromMaster;
    }

    @Override
    public void startToReplicateFrom(ReplicationMasterDescriptor master) throws IOException, ClassNotFoundException, JMSException {
        replicatingFromMaster = master;
        String uuid = registerReplicaWithMaster(master);
        TopicSubscriber replicationSubscription = master.getTopicSubscriber(uuid);
        URL initialLoadURL = master.getInitialLoadURL();
        final Replicator replicator = new Replicator(master, this, /* startSuspended */ true);
        replicationSubscription.setMessageListener(replicator);
        InputStream is = initialLoadURL.openStream();
        ObjectInputStream ois = new ObjectInputStream(is);
        getRacingEventService().initiallyFillFrom(ois);
        replicator.setSuspended(false); // apply queued operations
    }

    /**
     * @return the UUID that the master generated for this client which is also entered into {@link #replicaUUIDs}
     */
    private String registerReplicaWithMaster(ReplicationMasterDescriptor master) throws IOException, ClassNotFoundException {
        URL replicationRegistrationRequestURL = master.getReplicationRegistrationRequestURL();
        final URLConnection registrationRequestConnection = replicationRegistrationRequestURL.openConnection();
        registrationRequestConnection.connect();
        InputStream content = (InputStream) registrationRequestConnection.getContent();
        StringBuilder uuid = new StringBuilder();
        byte[] buf = new byte[256];
        int read = content.read(buf);
        while (read != -1) {
            uuid.append(new String(buf, 0, read));
            read = content.read(buf);
        }
        String replicaUUID = uuid.toString();
        registerReplicaUuidForMaster(replicaUUID, master);
        return replicaUUID;
    }

    @Override
    public <T> void executed(RacingEventServiceOperation<T> operation) {
        try {
            broadcastOperation(operation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void registerReplicaUuidForMaster(String uuid, ReplicationMasterDescriptor master) {
        replicaUUIDs.put(master, uuid);
    }

    @Override
    public Map<Class<? extends RacingEventServiceOperation<?>>, Integer> getStatistics(ReplicaDescriptor replicaDescriptor) {
        return replicationInstancesManager.getStatistics(replicaDescriptor);
    }

}
