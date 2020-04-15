package com.sap.sse.mongodb;

import com.mongodb.client.MongoDatabase;
import com.sap.sse.mongodb.internal.MongoDBServiceImpl;

public interface MongoDBService {

    MongoDBService INSTANCE = new MongoDBServiceImpl();

    MongoDatabase getDB();

    MongoDBConfiguration getConfiguration();

    /**
     * Should be called before accessing any collection.
     * 
     * @param registerForInterface
     *            The interface to register the collection for. This might be a placeholder Interface that is used by
     *            multiple classes accessing one and the same collection, demonstrating that they have knowledge of each
     *            other. This is resolved to a String representing the fully qualified class name to avoid issues when
     *            bundles are restarted, in which case the identity of class objects changes, but the fully qualified
     *            class name String remains the same.
     * @param collectionName
     * @throws AlreadyRegisteredException
     *             Is thrown if the collection name has already been registered for another class. This shouldn't happen
     *             in a productive version, but should make aware of the problem while developing.
     */
    void registerExclusively(Class<?> registerForInterface, String collectionName) throws AlreadyRegisteredException;
}
