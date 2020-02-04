package com.sap.sse.replication.persistence.impl;

import org.bson.Document;

import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.sap.sse.replication.ReplicaDescriptor;
import com.sap.sse.replication.persistence.MongoObjectFactory;

public class MongoObjectFactoryImpl implements MongoObjectFactory {
    private final MongoDatabase db;
    
    public MongoObjectFactoryImpl(MongoDatabase db) {
        super();
        this.db = db;
    }
    
    private MongoCollection<Document> getReplicaDescriptorCollection() {
        final MongoCollection<Document> result = db.getCollection(CollectionNames.REPLICA_DESCRIPTORS.name());
        result.createIndex(new Document(FieldNames.REPLICA_ID_AS_STRING.name(), 1));
        return result;
    }

    @Override
    public void storeReplicaDescriptor(ReplicaDescriptor replicaDescriptor) {
        final Document replicaDescriptorDoc = new Document();
        replicaDescriptorDoc.put(FieldNames.REPLICA_ID_AS_STRING.name(), replicaDescriptor.getUuid().toString());
        replicaDescriptorDoc.put(FieldNames.REPLICA_IP_ADDRESS.name(), replicaDescriptor.getIpAddress().getHostAddress());
        replicaDescriptorDoc.put(FieldNames.REPLICA_REGISTRATION_TIME_MILLIS.name(), replicaDescriptor.getRegistrationTime().asMillis());
        replicaDescriptorDoc.put(FieldNames.REPLICA_ADDITIONAL_INFORMATION.name(), replicaDescriptor.getAdditionalInformation());
        replicaDescriptorDoc.put(FieldNames.REPLICA_REPLICABLE_IDS_AS_STRINGS.name(), replicaDescriptor.getReplicableIdsAsStrings());
        getReplicaDescriptorCollection().withWriteConcern(WriteConcern.ACKNOWLEDGED).replaceOne(replicaDescriptorDoc,
                replicaDescriptorDoc, new UpdateOptions().upsert(true));
    }
    
    @Override
    public void removeReplicaDescriptor(ReplicaDescriptor replicaDescriptor) {
        if (replicaDescriptor != null) {
            final Document replicaDescriptorDoc = new Document(FieldNames.REPLICA_ID_AS_STRING.name(), replicaDescriptor.getUuid().toString());
            getReplicaDescriptorCollection().withWriteConcern(WriteConcern.ACKNOWLEDGED).deleteOne(replicaDescriptorDoc);
        }
    }
}
