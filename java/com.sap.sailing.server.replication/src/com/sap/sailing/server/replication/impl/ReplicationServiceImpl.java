package com.sap.sailing.server.replication.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.replication.ReplicaDescriptor;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;
import com.sap.sailing.server.replication.ReplicationService;

public class ReplicationServiceImpl implements ReplicationService {
    private final ReplicationInstancesManager replicationInstancesManager;
    
    private final MessageBrokerManager messageBrokerManager;
    
    private Topic replicationTopic;
    
    private ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    
    public ReplicationServiceImpl(final ReplicationInstancesManager replicationInstancesManager,
            final MessageBrokerManager messageBrokerManager) {
        this.replicationInstancesManager = replicationInstancesManager;
        this.messageBrokerManager = messageBrokerManager;
        racingEventServiceTracker = new ServiceTracker<RacingEventService, RacingEventService>(
                Activator.getDefaultContext(), RacingEventService.class.getName(), null);
        racingEventServiceTracker.open();
    }
    
    private RacingEventService getRacingEventService() {
        return racingEventServiceTracker.getService();
    }

    @Override
    public void registerReplica(ReplicaDescriptor replica) throws JMSException {
        Topic topic = getReplicationTopic();
        assert topic != null;
        replicationInstancesManager.registerReplica(replica);
    }
    
    @Override
    public void unregisterReplica(ReplicaDescriptor replica) {
        replicationInstancesManager.unregisterReplica(replica);
    }

    private Topic getReplicationTopic() throws JMSException{
        if (replicationTopic == null) {
            Session session = messageBrokerManager.getSession();
            if (session == null) {
                session = messageBrokerManager.createSession(true);
            }
            replicationTopic = session.createTopic("SailingServerReplicationTopic");
        }
        return replicationTopic;
    }
    
    public void broadcastOperation(RacingEventServiceOperation<?> operation) throws Exception {
        Topic topic = getReplicationTopic();
        Session session = messageBrokerManager.getSession();
        MessageProducer producer = messageBrokerManager.getSession().createProducer(topic);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        TextMessage message = session.createTextMessage("Hello World!");
        System.out.println("Sending message: " + message.getText());
        producer.send(message);
    }

    @Override
    public List<String> getHostnamesOfReplica() {
        List<String> result = new ArrayList<String>();
        for (Iterator<ReplicaDescriptor> i=replicationInstancesManager.getSlavesDescriptors(); i.hasNext(); ) {
            ReplicaDescriptor d = i.next();
            result.add(d.getIpAddress());
        }
        return result;
    }

    @Override
    public void startToReplicateFrom(ReplicationMasterDescriptor master) throws IOException, ClassNotFoundException, JMSException {
        registerReplicaWithMaster(master);
        TopicSubscriber replicationSubscription = messageBrokerManager.getSession().createDurableSubscriber(
                master.getReplicationTopic(), InetAddress.getLocalHost().getHostAddress());
        startReplicationObserver(replicationSubscription);
        URL initialLoadURL = master.getInitialLoadURL();
        InputStream is = initialLoadURL.openStream();
        ObjectInputStream ois = new ObjectInputStream(is);
        getRacingEventService().initiallyFillFrom(ois);
    }

    private void startReplicationObserver(TopicSubscriber replicationSubscription) {
        // TODO Auto-generated method stub
        
    }

    private void registerReplicaWithMaster(ReplicationMasterDescriptor master) throws IOException {
        URL replicationRegistrationRequestURL = master.getReplicationRegistrationRequestURL();
        final URLConnection registrationRequestConnection = replicationRegistrationRequestURL.openConnection();
        registrationRequestConnection.connect();
        registrationRequestConnection.getContent();
    }
    
}
