package com.sap.sse.landscape.aws.persistence.impl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.Binary;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sap.sse.common.TimePoint;
import com.sap.sse.landscape.aws.persistence.DomainObjectFactory;
import com.sap.sse.landscape.ssh.SSHKeyPair;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private final MongoDatabase db;

    public DomainObjectFactoryImpl(MongoDatabase db) {
        this.db = db;
    }
    
    @Override
    public Iterable<SSHKeyPair> loadSSHKeyPairs() {
        final List<SSHKeyPair> result = new ArrayList<>();
        final MongoCollection<org.bson.Document> keyPairCollection = db.getCollection(CollectionNames.SSH_KEY_PAIRS.name());
        keyPairCollection.createIndex(new Document(FieldNames.SSH_KEY_PAIR_REGION_ID.name(), 1));
        for (Object o : keyPairCollection.find()) {
            final String regionId = (String) ((Document) o).get(FieldNames.SSH_KEY_PAIR_REGION_ID.name());
            final String creatorName = (String) ((Document) o).get(FieldNames.SSH_KEY_PAIR_CREATOR_NAME.name());
            final long creationDateMillis = ((Number) ((Document) o).get(FieldNames.SSH_KEY_PAIR_CREATION_DATE.name())).longValue();
            final String name = (String) ((Document) o).get(FieldNames.SSH_KEY_PAIR_NAME.name());
            final Binary privKey = (Binary) ((Document) o).get(FieldNames.SSH_KEY_PAIR_ENCRYPTED_PRIVATE_KEY.name());
            final byte[] privateKey = privKey==null?null:privKey.getData();
            final Binary pubKey = (Binary)  ((Document) o).get(FieldNames.SSH_KEY_PAIR_PUBLIC_KEY.name());
            final byte[] publicKey = pubKey==null?null:pubKey.getData();
            result.add(new SSHKeyPair(regionId, creatorName, TimePoint.of(creationDateMillis), name, publicKey, privateKey));
        }
        return result;
    }
}
