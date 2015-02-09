package com.sap.sse.security.replication.test;

import java.io.IOException;
import java.net.MalformedURLException;

import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.replication.testsupport.AbstractServerReplicationTest;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.impl.SecurityServiceImpl;
import com.sap.sse.security.userstore.mongodb.MongoObjectFactory;
import com.sap.sse.security.userstore.mongodb.PersistenceFactory;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;

public abstract class AbstractSecurityReplicationTest extends AbstractServerReplicationTest<SecurityService, SecurityServiceImpl> {
    private MongoDBService mongoDBService;
    private MongoObjectFactory mongoObjectFactory;

    @Override
    protected void persistenceSetUp(boolean dropDB) {
        mongoDBService = MongoDBService.INSTANCE;
        if (dropDB) {
            mongoDBService.getDB().dropDatabase();
        }
        mongoObjectFactory = PersistenceFactory.INSTANCE.getMongoObjectFactory(mongoDBService);
        mongoObjectFactory.getDatabase().requestStart();
    }

    @Override
    protected SecurityServiceImpl createNewMaster() throws MalformedURLException, IOException, InterruptedException {
        SecurityServiceImpl result = new SecurityServiceImpl(new UserStoreImpl());
        result.clearReplicaState();
        return result;
    }

    @Override
    protected SecurityServiceImpl createNewReplica() {
        return new SecurityServiceImpl(new UserStoreImpl());
    }

    @Override
    protected void persistenceTearDown() {
        mongoObjectFactory.getDatabase().requestDone();
    }
}
