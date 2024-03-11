package com.sap.sse.mongodb;

import java.lang.ref.WeakReference;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.sap.sse.common.Duration;
import com.sap.sse.mongodb.internal.MongoDBServiceImpl;

public interface MongoDBService {

    Duration SESSION_REFERSH_INTERVAL = Duration.ONE_MINUTE;
    
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

    /**
     * Obtains a {@link ClientSession} for the MongoDB configuration used by this service. See also
     * {@link MongoClient#startSession()}. Such a session will time out on the server after some time,
     * removing all cursors that are still open. The regular server-side session timeout is 30 minutes.
     * The timeout applies if the client doesn't request anything from the server within the session
     * during the timeout period. For example, if the client takes more than 30min to process what a
     * single batch of cursor results of a {@code find} query returned, the session and with it the cursor
     * will die.<p>
     * 
     * To avoid this sort of problem, sessions returned by this call will internally be wrapped by a
     * {@link WeakReference} that is stored in a cache. All sessions store this way and still reachable
     * through the weak reference will be refreshed by a background timer every {@link #SESSION_REFERSH_INTERVAL}.
     * Those weak references that got cleared will be removed from the cache.
     */
    ClientSession startAutoRefreshingSession();

    ClientSession startCausallyConsistentSession();

    MongoClient getMongoClient();
}
