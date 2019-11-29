package com.sap.sse.security.storemerging;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.impl.Role;
import com.sap.sse.security.shared.impl.User;
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
        // the following maps work like this: The keys are source objects to be imported.
        // If the key object is to be added to the target, it is its own value;
        // if it is to be dropped, the key is not part of the map. If it is to be merged with an object in the target,
        // the corresponding target object is the value.
        final Map<User, User> userMap = markUsersForAddMergeOrDrop(sourceUserStore);
        final Map<UserGroup, UserGroup> userGroupMap = markUserGroupsForAddMergeOrDrop(sourceUserStore);
        replaceSourceUserReferencesToUsersAndGroups(userMap, userGroupMap);
        replaceSourceUserGroupReferencesToUsers(userMap, userGroupMap);
        replaceSourceOwnershipReferencesToUsersAndGroups(sourceAccessControlStore, userMap, userGroupMap);
        replaceSourceAccessControlListReferencesToGroups(sourceAccessControlStore, userGroupMap);
        mergeUsersAndGroups(sourceUserStore, userMap, userGroupMap);
        mergePreferences(sourceUserStore, userMap);
        mergeOwnerships(sourceAccessControlStore, userMap, userGroupMap);
        mergeAccessControlLists(sourceAccessControlStore, userGroupMap);
        return new Pair<>(sourceUserStore, sourceAccessControlStore);
    }

    private Map<User, User> markUsersForAddMergeOrDrop(UserStore sourceUserStore) {
        final Map<User, User> userMap = new HashMap<>();
        for (final User user : sourceUserStore.getUsers()) {
            final User targetUserWithEqualName = targetUserStore.getUserByName(user.getName());
            if (targetUserWithEqualName != null) {
                if (Util.equalsWithNull(user.getEmail(), targetUserWithEqualName.getEmail())) {
                    logger.info("Found user "+user.getName()+" in target having equal e-mail address "+user.getEmail()+
                            ". Marking for merge.");
                    userMap.put(user, targetUserWithEqualName);
                } else {
                    logger.info("Found user " + user.getName() + " in target, but e-mail addresses " + user.getEmail()
                            + " and " + targetUserWithEqualName.getEmail() + " don't match. Dropping.");
                }
            } else {
                logger.info("User "+user.getName()+" not found in target. Marking for adding.");
                userMap.put(user, user);
            }
        }
        return userMap;
    }

    private Map<UserGroup, UserGroup> markUserGroupsForAddMergeOrDrop(UserStore sourceUserStore) {
        final Map<UserGroup, UserGroup> userGroupMap = new HashMap<>();
        for (final UserGroup sourceGroup : sourceUserStore.getUserGroups()) {
            final UserGroup targetGroupWithSameID = targetUserStore.getUserGroup(sourceGroup.getId());
            if (targetGroupWithSameID != null) {
                logger.info("Identical target group found: "+targetGroupWithSameID+". Marking for merge.");
                userGroupMap.put(sourceGroup, targetGroupWithSameID);
            } else {
                final UserGroup targetGroupWithEqualName = targetUserStore.getUserGroupByName(sourceGroup.getName());
                if (targetGroupWithEqualName != null) {
                    if (considerGroupsIdentical(targetGroupWithEqualName, sourceGroup)) {
                        logger.info("Identical target group (though different ID) found: "+targetGroupWithEqualName+". Merging...");
                        userGroupMap.put(sourceGroup, targetGroupWithEqualName);
                    } else {
                        logger.warning("Found existing target user group "+targetGroupWithEqualName+" but source group "+
                                sourceGroup+" is not considered identical. Dropping.");
                    }
                } else {
                    logger.info("No target user group found for source group "+sourceGroup+". Marking for adding");
                    userGroupMap.put(sourceGroup, sourceGroup);
                }
            }
        }
        return userGroupMap;
    }

    private void replaceSourceUserReferencesToUsersAndGroups(Map<User, User> userMap,
            Map<UserGroup, UserGroup> userGroupMap) {
        for (final User sourceUser : userMap.keySet()) {
            final Set<Role> rolesToRemoveBecauseOfLostOrMissingQualifier = new HashSet<>();
            final Map<Role, Role> rolesToReplaceDueToChangingQualifierObject = new HashMap<>();
            for (final Role role : sourceUser.getRoles()) {
                final User qualifiedForUser = role.getQualifiedForUser();
                final UserGroup qualifiedForGroup = role.getQualifiedForTenant();
                if (qualifiedForUser == null && qualifiedForGroup == null) {
                    logger.severe("Dropping unqualified role "+role+" from user "+sourceUser);
                    rolesToRemoveBecauseOfLostOrMissingQualifier.add(role);
                } else {
                    final User userQualifierInTarget;
                    final UserGroup groupQualifierInTarget;
                    if (qualifiedForUser != null) {
                        userQualifierInTarget = userMap.get(qualifiedForUser);
                        if (userQualifierInTarget == null) {
                            logger.severe("User qualifying role "+role+" for user "+sourceUser+" will be dropped. Dropping role.");
                            rolesToRemoveBecauseOfLostOrMissingQualifier.add(role);
                        }
                    } else {
                        userQualifierInTarget = null;
                    }
                    if (qualifiedForGroup != null) {
                        groupQualifierInTarget = userGroupMap.get(qualifiedForGroup);
                        if (groupQualifierInTarget == null) {
                            logger.severe("Group qualifying role "+role+" for user "+sourceUser+" will be dropped. Dropping role.");
                            rolesToRemoveBecauseOfLostOrMissingQualifier.add(role);
                        }
                    } else {
                        groupQualifierInTarget = null;
                    }
                    if (!rolesToRemoveBecauseOfLostOrMissingQualifier.contains(role) &&
                            (qualifiedForUser != userQualifierInTarget || qualifiedForGroup != groupQualifierInTarget)) {
                        logger.info("Qualifying user/group for role "+role+" on user "+sourceUser+
                                " merged to target. Updating role");
                        rolesToReplaceDueToChangingQualifierObject.put(role,
                                new Role(role.getRoleDefinition(), groupQualifierInTarget, userQualifierInTarget));
                    }
                }
            }
            for (final Role roleToRemove : rolesToRemoveBecauseOfLostOrMissingQualifier) {
                sourceUser.removeRole(roleToRemove);
            }
            for (final Entry<Role, Role> e : rolesToReplaceDueToChangingQualifierObject.entrySet()) {
                sourceUser.removeRole(e.getKey());
                sourceUser.addRole(e.getValue());
            }
        }
    }
    
    private void replaceSourceUserGroupReferencesToUsers(Map<User, User> userMap,
            Map<UserGroup, UserGroup> userGroupMap) {
        // two passes to avoid ConcurrentModificationException
        for (final UserGroup sourceUserGroup : userGroupMap.keySet()) {
            final Map<User, User> mapping = new HashMap<>();
            for (final User user : sourceUserGroup.getUsers()) {
                mapping.put(user, userMap.get(user));
            }
            for (final Entry<User, User> e : mapping.entrySet()) {
                sourceUserGroup.remove(e.getKey());
                if (e.getValue() == null) {
                    logger.severe("User "+e.getKey()+" from group "+sourceUserGroup+" dropped. Removing from group.");
                } else if (e.getValue() != e.getKey()) {
                    logger.info("User "+e.getKey()+" from group "+sourceUserGroup+" merging into target's user "+e.getValue()+
                            ". Updating group.");
                    sourceUserGroup.add(e.getValue());
                }
            }
        }
    }
    
    private void replaceSourceOwnershipReferencesToUsersAndGroups(AccessControlStore sourceAccessControlStore,
            Map<User, User> userMap, Map<UserGroup, UserGroup> userGroupMap) {
        for (final OwnershipAnnotation sourceOwnership : sourceAccessControlStore.getOwnerships()) {
            final UserGroup groupOwnership = sourceOwnership.getAnnotation().getTenantOwner();
            
        }
        // TODO Implement SecurityStoreMerger.replaceSourceOwnershipReferencesToUsersAndGroups(...)
        
    }
    
    private void replaceSourceAccessControlListReferencesToGroups(AccessControlStore sourceAccessControlStore,
            Map<UserGroup, UserGroup> userGroupMap) {
        // TODO Implement SecurityStoreMerger.replaceSourceAccessControlListReferencesToGroups(...)
        
    }

    private void mergeAccessControlLists(AccessControlStore sourceAccessControlStore, Map<UserGroup, UserGroup> userGroupMap) {
        // TODO Implement Main.mergeAccessControlLists(...)
        
    }

    private void mergeOwnerships(AccessControlStore sourceAccessControlStore, Map<User, User> userMap, Map<UserGroup, UserGroup> userGroupMap) {
        // TODO Implement Main.mergeOwnerships(...)
        
    }

    private void mergeUsersAndGroups(UserStore sourceUserStore, Map<User, User> userMap, Map<UserGroup, UserGroup> userGroupMap) {
        for (final UserGroup sourceGroup : sourceUserStore.getUserGroups()) {
            final UserGroup targetGroupWithSameID = targetUserStore.getUserGroup(sourceGroup.getId());
            if (targetGroupWithSameID != null) {
                logger.info("Identical target group found: "+targetGroupWithSameID+". Merging...");
                mergeSecondUserGroupIntoFirst(targetGroupWithSameID, sourceGroup);
            } else {
                final UserGroup targetGroupWithEqualName = targetUserStore.getUserGroupByName(sourceGroup.getName());
                if (targetGroupWithEqualName != null) {
                    if (considerGroupsIdentical(targetGroupWithEqualName, sourceGroup)) {
                        logger.info("Identical target group (though different ID) found: "+targetGroupWithEqualName+". Merging...");
                        mergeSecondUserGroupIntoFirst(targetGroupWithEqualName, sourceGroup);
                    } else {
                        logger.warning("Found existing target user group "+targetGroupWithEqualName+" but source group "+
                                sourceGroup+" is not considered identical, so not merging");
                    }
                } else {
                    logger.info("No target user group found for source group "+sourceGroup+". Mering for adding");
                }
            }
        }
        // TODO Implement Main.mergeUsersAndGroups(...)
        
    }
    
    private void mergeSecondUserGroupIntoFirst(UserGroup targetGroupWithSameID, UserGroup sourceGroup) {
        // TODO Implement SecurityStoreMerger.mergeSecondUserGroupIntoFirst(...)
        
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

    private void mergePreferences(UserStore sourceUserStore, Map<User, User> userMap) {
        // TODO Implement Main.mergePreferences(...)
        
    }
}
