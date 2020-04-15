package com.sap.sse.replication.persistence.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.replication.ReplicaDescriptor;
import com.sap.sse.replication.impl.ReplicaDescriptorImpl;
import com.sap.sse.replication.persistence.DomainObjectFactory;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private static final Logger logger = Logger.getLogger(DomainObjectFactoryImpl.class.getName());
    private final MongoDatabase db;
    
    public DomainObjectFactoryImpl(MongoDatabase db) {
        super();
        this.db = db;
    }

    private MongoCollection<Document> getReplicaDescriptorCollection() {
        final MongoCollection<Document> result = db.getCollection(CollectionNames.REPLICA_DESCRIPTORS.name());
        return result;
    }

    @Override
    public Iterable<ReplicaDescriptor> loadReplicaDescriptors() {
        final Map<UUID, ReplicaDescriptor> result = new HashMap<>();
        for (final Document o : getReplicaDescriptorCollection().find()) {
            final ReplicaDescriptor replicaDescriptor = loadReplicaDescriptor(o);
            if (replicaDescriptor != null) {
                result.put(replicaDescriptor.getUuid(), replicaDescriptor);
            }
        }
        return result.values();
    }

    private ReplicaDescriptor loadReplicaDescriptor(Document o) {
        final UUID uuid = UUID.fromString(o.getString(FieldNames.REPLICA_ID_AS_STRING.name()));
        InetAddress ipAddress;
        String ipAddressString = o.getString(FieldNames.REPLICA_IP_ADDRESS.name());
        try {
            ipAddress = InetAddress.getByName(ipAddressString);
        } catch (UnknownHostException e) {
            final String message = "Internal error: the IP address "+ipAddressString+
                    " cannot be converted into an InetAddress";
            logger.log(Level.WARNING, message, e);
            throw new RuntimeException(message, e);
        }
        final TimePoint registrationTime = new MillisecondsTimePoint(o.getLong(FieldNames.REPLICA_REGISTRATION_TIME_MILLIS.name()));
        final String additionalInformation = o.getString(FieldNames.REPLICA_ADDITIONAL_INFORMATION.name());
        final String[] replicableIdsAsStrings = o.get(FieldNames.REPLICA_REPLICABLE_IDS_AS_STRINGS.name(), new ArrayList<String>()).toArray(new String[0]);
        return new ReplicaDescriptorImpl(ipAddress, uuid, registrationTime, additionalInformation, replicableIdsAsStrings);
    }
}
