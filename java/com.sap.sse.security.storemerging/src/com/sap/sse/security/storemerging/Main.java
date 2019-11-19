package com.sap.sse.security.storemerging;

import java.util.logging.Logger;

import com.mongodb.MongoClientURI;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.security.interfaces.AccessControlStore;
import com.sap.sse.security.interfaces.UserStore;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.userstore.mongodb.AccessControlStoreImpl;
import com.sap.sse.security.userstore.mongodb.PersistenceFactory;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    
    private static final String TARGET_DEFAULT_TENANT_NAME_SYSTEM_PROPERTY_NAME = "default.tenant.name";

    private final UserStore targetUserStore;
    private final AccessControlStore targetAccessControlStore;

    public Main(MongoDBConfiguration cfgForTarget) throws UserGroupManagementException, UserManagementException {
        final PersistenceFactory targetPf = PersistenceFactory.create(cfgForTarget.getService());
        logger.info("Loading target user store from "+cfgForTarget);
        this.targetUserStore = loadUserStore(targetPf, System.getProperty(TARGET_DEFAULT_TENANT_NAME_SYSTEM_PROPERTY_NAME));
        logger.info("Loading target access control store from "+cfgForTarget);
        final AccessControlStore accessControlStore = loadAccessControlStore(targetPf, targetUserStore);
        this.targetAccessControlStore = accessControlStore;
    }

    private AccessControlStore loadAccessControlStore(final PersistenceFactory targetPf, UserStore userStore) {
        final AccessControlStore accessControlStore = new AccessControlStoreImpl(targetPf.getDefaultDomainObjectFactory(), targetPf.getDefaultMongoObjectFactory(), userStore);
        accessControlStore.loadACLsAndOwnerships();
        return accessControlStore;
    }

    private UserStoreImpl loadUserStore(final PersistenceFactory targetPf, String defaultTenantName)
            throws UserGroupManagementException, UserManagementException {
        final UserStoreImpl userStore = new UserStoreImpl(targetPf.getDefaultDomainObjectFactory(),
                targetPf.getDefaultMongoObjectFactory(), defaultTenantName);
        userStore.ensureDefaultRolesExist();
        // actually load the users and migrate them if required
        userStore.loadAndMigrateUsers();
        return userStore;
    }

    /**
     * The target MongoDB into which to merge is provided by the system properties {@code mongo.uri} or alternatively
     * {@code mongo.port}, {@code mongo.host} and {@code mongo.dbName}. See also {@link MongoDBConfiguration}. The
     * source databases from which to import, as well as their default creation group names are provided as pairs of
     * command line arguments to this main method where the first element of each pair is the MongoDB URI indicating the
     * database from which from read, and the second element being the name of the store's corresponding default
     * creation group name (we don't expect this to be used, still it needs to be provided for consistency).<p>
     * 
     * Order matters. The import will happen source by source, as if you executed those imports one after the other.
     * Clash / conflict resolution then works as specified for a single import.<p>
     * 
     * The importing environment's default creation group has to be provided in the system property named as defined
     * by {@link #TARGET_DEFAULT_TENANT_NAME_SYSTEM_PROPERTY_NAME}.<p>
     * 
     * Example:<br>
     *  {@code java -Dmongo.uri="mongodb://localhost/winddb" -Ddefault.tenant.name=my-group} {@link Main} {@code "mongodb://localhost:27017/winddb2" my-default-group "mongodb://otherhost:12345/winddb3?replicaSet=rs0" another-group}
     * 
     * @param args
     *            pairs of MongoDB URI and default creation group names for the stores from which to import
     */
    public static void main(String[] args) throws UserGroupManagementException, UserManagementException {
        final MongoDBConfiguration cfgForTarget = MongoDBConfiguration.getDefaultConfiguration();
        final Main instance = new Main(cfgForTarget);
        for (int i=0; i<args.length/2; i++) {
            final MongoDBConfiguration cfgForSource = new MongoDBConfiguration(new MongoClientURI(args[2*i]));
            instance.importStores(cfgForSource, args[2*i+1]);
        }
    }

    private void importStores(MongoDBConfiguration cfgForSource, String defaultCreationGroupNameForSource) throws UserGroupManagementException, UserManagementException {
        logger.info("Importing user store and access control store read from "+cfgForSource);
        final PersistenceFactory sourcePf = PersistenceFactory.create(cfgForSource.getService());
        final UserStore sourceUserStore = loadUserStore(sourcePf, defaultCreationGroupNameForSource);
        final AccessControlStore sourceAccessControlStore = loadAccessControlStore(sourcePf, sourceUserStore);
        mergeUsersAndGroups(sourceUserStore);
        mergePreferences(sourceUserStore);
        mergeOwnerships(sourceAccessControlStore);
        mergeAccessControlLists(sourceAccessControlStore);
    }

    private void mergeAccessControlLists(AccessControlStore sourceAccessControlStore) {
        // TODO Implement Main.mergeAccessControlLists(...)
        
    }

    private void mergeOwnerships(AccessControlStore sourceAccessControlStore) {
        // TODO Implement Main.mergeOwnerships(...)
        
    }

    private void mergeUsersAndGroups(UserStore sourceUserStore) {
        // TODO Implement Main.mergeUsersAndGroups(...)
        
    }

    private void mergePreferences(UserStore sourceUserStore) {
        // TODO Implement Main.mergePreferences(...)
        
    }
}
