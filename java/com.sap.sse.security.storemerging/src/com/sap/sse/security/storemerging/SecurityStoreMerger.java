package com.sap.sse.security.storemerging;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.MongoClientURI;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.interfaces.AccessControlStore;
import com.sap.sse.security.interfaces.UserStore;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.impl.UserGroup;
import com.sap.sse.security.userstore.mongodb.AccessControlStoreImpl;
import com.sap.sse.security.userstore.mongodb.PersistenceFactory;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;

public class SecurityStoreMerger {
    private static final Logger logger = Logger.getLogger(SecurityStoreMerger.class.getName());
    
    private static final String TARGET_DEFAULT_TENANT_NAME_SYSTEM_PROPERTY_NAME = "default.tenant.name";

    private final UserStore targetUserStore;
    private final AccessControlStore targetAccessControlStore;

    public SecurityStoreMerger(MongoDBConfiguration cfgForTarget, String targetDefaultCreationGroupName) throws UserGroupManagementException, UserManagementException {
        final PersistenceFactory targetPf = PersistenceFactory.create(cfgForTarget.getService());
        logger.info("Loading target user store from "+cfgForTarget);
        this.targetUserStore = loadUserStore(targetPf, targetDefaultCreationGroupName);
        logger.info("Loading target access control store from "+cfgForTarget);
        this.targetAccessControlStore = loadAccessControlStore(targetPf, targetUserStore);
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

    public UserStore getTargetUserStore() {
        return targetUserStore;
    }

    public AccessControlStore getTargetAccessControlStore() {
        return targetAccessControlStore;
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
     *  {@code java -Dmongo.uri="mongodb://localhost/winddb" -Ddefault.tenant.name=my-group} {@link SecurityStoreMerger} {@code "mongodb://localhost:27017/winddb2" my-default-group "mongodb://otherhost:12345/winddb3?replicaSet=rs0" another-group}
     * 
     * @param args
     *            pairs of MongoDB URI and default creation group names for the stores from which to import
     */
    public static void main(String[] args) throws UserGroupManagementException, UserManagementException {
        final MongoDBConfiguration cfgForTarget = MongoDBConfiguration.getDefaultConfiguration();
        final SecurityStoreMerger instance = new SecurityStoreMerger(cfgForTarget, System.getProperty(TARGET_DEFAULT_TENANT_NAME_SYSTEM_PROPERTY_NAME));
        for (int i=0; i<args.length/2; i++) {
            final MongoDBConfiguration cfgForSource = new MongoDBConfiguration(new MongoClientURI(args[2*i]));
            instance.importStores(cfgForSource, args[2*i+1]);
        }
    }

    Pair<UserStore, AccessControlStore> importStores(MongoDBConfiguration cfgForSource, String defaultCreationGroupNameForSource) throws UserGroupManagementException, UserManagementException {
        logger.info("Importing user store and access control store read from "+cfgForSource);
        final PersistenceFactory sourcePf = PersistenceFactory.create(cfgForSource.getService());
        final UserStore sourceUserStore = loadUserStore(sourcePf, defaultCreationGroupNameForSource);
        final AccessControlStore sourceAccessControlStore = loadAccessControlStore(sourcePf, sourceUserStore);
        mergeUsersAndGroups(sourceUserStore);
        mergePreferences(sourceUserStore);
        mergeOwnerships(sourceAccessControlStore);
        mergeAccessControlLists(sourceAccessControlStore);
        return new Pair<>(sourceUserStore, sourceAccessControlStore);
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
    
    /**
     * If the groups have equal {@link UserGroup#getId() IDs} then they are considered identical. If both groups have
     * different IDs but equal names and the names match the pattern {@code <username>-tenant} and both contain a user
     * named {@code <username>} then they will be considered identical, too. In all other cases they are considered
     * distinct.
     */
    static boolean considerGroupsIdentical(final UserGroup g1, final UserGroup g2) {
        final String g1TenantGroupUserName, g2TenantGroupUserName;
        return g1.getId().equals(g2.getId()) ||
                (g1TenantGroupUserName=getTenantGroupUserName(g1)) != null &&
                (g2TenantGroupUserName=getTenantGroupUserName(g2)) != null &&
                g1TenantGroupUserName.equals(g2TenantGroupUserName) &&
                hasUserNamed(g1, g1TenantGroupUserName) &&
                hasUserNamed(g2, g2TenantGroupUserName);
    }

    private static boolean hasUserNamed(UserGroup group, String username) {
        return Util.contains(Util.map(group.getUsers(), u->u.getName()), username);
    }

    private static final Pattern tenantUserGroupNamePattern = Pattern.compile("(.*)"+SecurityService.TENANT_SUFFIX);
    private static String getTenantGroupUserName(UserGroup g) {
        final String result;
        final Matcher matcher = tenantUserGroupNamePattern.matcher(g.getName());
        if (matcher.matches()) {
            result = matcher.group(1);
        } else {
            result = null;
        }
        return result;
    }

    private void mergePreferences(UserStore sourceUserStore) {
        // TODO Implement Main.mergePreferences(...)
        
    }
}
