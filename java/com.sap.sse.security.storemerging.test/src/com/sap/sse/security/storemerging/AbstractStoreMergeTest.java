package com.sap.sse.security.storemerging;

import java.io.IOException;
import java.util.UUID;

import org.bson.Document;
import org.junit.After;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.security.interfaces.AccessControlStore;
import com.sap.sse.security.interfaces.UserStore;
import com.sap.sse.security.shared.UserStoreManagementException;
import com.sap.sse.security.storemerging.test.MongoDBFiller;
import com.sap.sse.security.userstore.mongodb.impl.CollectionNames;

/**
 * To create a new test suite based on a test frame, create source and target DB content. Start by running
 * {@code resources/export.sh} by providing a variant name, such as {@code target_MyNewTest} and a database name that
 * does not exist, such as {@code nonexisting}. This will produce the files you need to start with an empty DB. Then run
 * {@code resources/import.sh} with this new variant in order to clear the relevant collections:
 * {@code import.sh target_MyNewTest winddb}. Note that this will unrecoverably clear a few collections in that DB, so
 * make sure to use some "volatile" test/dev DB.
 * <p>
 * 
 * Do the same for the source configuration, replacing {@code target} by {@code source} in the above.
 * <p>
 * 
 * Then start your local server from the DB in which you cleared the collections this way. Create your target set-up
 * regarding users, groups, roles, ownerships, access control lists, and preferences. Then run the {@code export.sh}
 * script again.
 * <p>
 * 
 * Then import the the empty {@code source} variant again and restart your server. Create the set-up that you would like
 * the test to import as source into your target that you had set up previously. Then export to the source variant,
 * e.g., with {@code export.sh source_MyNewTest winddb}.
 * <p>
 * 
 * Now create your test case, e.g., {@code MyNewTest} with a {@code @Before} method that calls this class'
 * {@link #setUp(String, String)} method with the source and target variant names:
 * 
 * <pre>
 * &#64;Before
 * public void setUp() throws IOException {
 *     setUp("source_MyNewTest", "target_MyNewTest");
 * }
 * </pre>
 * 
 * After the {@link #setUp(String, String)} method has completed normally, the {@link #merger} will have loaded the
 * target user store which is assigned to {@link #targetUserStore}, and the target access control store which is
 * assigned to {@link #targetAccessControlStore}. Implement assertions against the original, unmodified target stores to
 * validate your assumptions about what should be in the unmodified target. Read the source stores using
 * {@link #readSourceStores()}. This returns the original contents of the source user and access control stores, not yet
 * modified by any merge action. You can implement assertions against these source stores.
 * <p>
 * 
 * Then trigger the merge, using {@link #mergeSourceIntoTarget(UserStore, AccessControlStore)}, passing the two source
 * stores. Afterwards, implement assertions based on the target stores ({@link #targetUserStore} and
 * {@link #targetAccessControlStore}).
 * 
 * @author Axel Uhl (d043530)
 *
 */
public abstract class AbstractStoreMergeTest {
    private final static String importSourceMongoDbUri = MongoDBConfiguration.getDefaultTestConfiguration()
            .getMongoClientURI().getURI()
            .replace(MongoDBConfiguration.getDefaultTestConfiguration().getMongoClientURI().getDatabase(),
                    UUID.randomUUID().toString());
    protected final static String defaultCreationGroupNameForSource = "dummy-default-creation-group-for-source";
    protected final static String defaultCreationGroupNameForTarget = "dummy-default-creation-group-for-target";
    protected MongoDBConfiguration cfgForSource;
    private MongoDatabase targetDb;
    protected MongoDBConfiguration cfgForTarget;
    protected SecurityStoreMerger merger;
    protected UserStore targetUserStore;
    protected AccessControlStore targetAccessControlStore;

    protected void setUp(String sourceVariant, String targetVariant) throws IOException, UserStoreManagementException {
        cfgForTarget = MongoDBConfiguration.getDefaultTestConfiguration();
        targetDb = cfgForTarget.getService().getDB();
        fill(targetVariant, targetDb);
        cfgForSource = new MongoDBConfiguration(new MongoClientURI(importSourceMongoDbUri));
        fill(sourceVariant, cfgForSource.getService().getDB());
        merger = new SecurityStoreMerger(cfgForTarget, defaultCreationGroupNameForTarget);
        targetUserStore = merger.getTargetUserStore();
        targetAccessControlStore = merger.getTargetAccessControlStore();
    }
    
    @After
    public void tearDown() {
        cfgForSource.getService().getDB().drop();
    }

    private void fill(final String variant, final MongoDatabase db) throws IOException {
        for (final CollectionNames collectionName : new CollectionNames[] { CollectionNames.USERS,
                CollectionNames.USER_GROUPS, CollectionNames.OWNERSHIPS, CollectionNames.ACCESS_CONTROL_LISTS,
                CollectionNames.PREFERENCES, CollectionNames.ROLES }) {
            fill(collectionName, variant, db);
        }
    }

    private void fill(final CollectionNames collectionName, final String variant, final MongoDatabase db)
            throws IOException {
        final MongoDBFiller filler = new MongoDBFiller();
        final MongoCollection<Document> collection = db.getCollection(collectionName.name());
        collection.drop();
        filler.fill(collection, "/resources/"+collectionName.name()+"_"+variant+".json");
    }

    /**
     * Reads the user store and the access control store from {@link #cfgForSource} and returns them. This allows tests
     * to validate the source stores' contents before the actual
     * {@link #mergeSourceIntoTarget(UserStore, AccessControlStore) merge step} which is helpful because the merge may
     * modify objects loaded from the source stores in memory during the merge.
     * 
     * @return the source user store and the source access control store in their original, merge-unmodified version
     */
    protected Pair<UserStore, AccessControlStore> readSourceStores() throws UserStoreManagementException {
        return merger.readStores(cfgForSource, defaultCreationGroupNameForSource);
    }

    /**
     * Merges the user store and the access control store into the {@link #merger}'s target stores that are backed by
     * the {@link #cfgForTarget} DB configuration. The result of the merge can be seen in the {@link #merger}'s
     * {@link SecurityStoreMerger#getTargetUserStore()} and {@link SecurityStoreMerger#getTargetAccessControlStore()}.
     */
    protected void mergeSourceIntoTarget(final UserStore sourceUserStore,
            final AccessControlStore sourceAccessControlStore)
            throws UserStoreManagementException {
        merger.importStores(sourceUserStore, sourceAccessControlStore);
    }
}
