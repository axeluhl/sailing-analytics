package com.sap.sse.landscape.aws.persistence.impl;

import org.bson.Document;

import com.mongodb.WriteConcern;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.sap.sse.landscape.aws.persistence.MongoObjectFactory;
import com.sap.sse.landscape.ssh.SSHKeyPair;

public class MongoObjectFactoryImpl implements MongoObjectFactory {
    private final MongoDatabase db;

    public MongoObjectFactoryImpl(MongoDatabase db) {
        this.db = db;
    }
    
    @Override
    public void clear() {
        db.getCollection(CollectionNames.SSH_KEY_PAIRS.name()).withWriteConcern(WriteConcern.ACKNOWLEDGED).drop();
    }

    @Override
    public void storeSSHKeyPair(SSHKeyPair keyPair) {
        final Document query = getSSHKeyPairDBQuery(keyPair.getRegionId(), keyPair.getName());
        final Document keyPairDocument = getSSHKeyPairDBQuery(keyPair.getRegionId(), keyPair.getName());
        keyPairDocument.put(FieldNames.SSH_KEY_PAIR_CREATOR_NAME.name(), keyPair.getCreatorName());
        keyPairDocument.put(FieldNames.SSH_KEY_PAIR_CREATION_DATE.name(), keyPair.getCreationTime().asMillis());
        keyPairDocument.put(FieldNames.SSH_KEY_PAIR_PUBLIC_KEY.name(), keyPair.getPublicKey());
        keyPairDocument.put(FieldNames.SSH_KEY_PAIR_PRIVATE_KEY.name(), keyPair.getPrivateKey());
        db.getCollection(CollectionNames.SSH_KEY_PAIRS.name()).withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .replaceOne(query, keyPairDocument, new UpdateOptions().upsert(true));
    }

    private Document getSSHKeyPairDBQuery(String regionId, String keyName) {
        final Document basicDBObject = new Document(FieldNames.SSH_KEY_PAIR_REGION_ID.name(), regionId.toString());
        basicDBObject.put(FieldNames.SSH_KEY_PAIR_NAME.name(), keyName);
        return basicDBObject;
    }
    
    @Override
    public void removeSSHKeyPair(String regionId, String keyName) {
        final Document basicDBObject = getSSHKeyPairDBQuery(regionId, keyName);
        db.getCollection(CollectionNames.SSH_KEY_PAIRS.name()).withWriteConcern(WriteConcern.ACKNOWLEDGED).deleteOne(basicDBObject);
    }
}
