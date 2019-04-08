package com.sap.sailing.xmlexport.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.servlet.ServletContext;

import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.mgt.SecurityManager;

import com.sap.sse.common.mail.MailException;
import com.sap.sse.replication.OperationExecutionListener;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.OperationWithResultWithIdWrapper;
import com.sap.sse.replication.OperationsToMasterSender;
import com.sap.sse.replication.OperationsToMasterSendingQueue;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.security.Action;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.interfaces.Credential;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.RolePrototype;
import com.sap.sse.security.shared.SocialUserAccount;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;
import com.sap.sse.security.shared.impl.AccessControlList;
import com.sap.sse.security.shared.impl.Ownership;
import com.sap.sse.security.shared.impl.Role;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;

/**
 * Mock of SecurityService which does not throw exception on
 * checkCurrentUserReadPermission(WithQualifiedObjectIdentifier) and returns true for
 * hasCurrentUserReadPermission(WithQualifiedObjectIdentifier).
 */
@SuppressWarnings("restriction")
public class SecurityServiceMock implements SecurityService {

    @Override
    public ObjectInputStream createObjectInputStreamResolvingAgainstCache(InputStream is) throws IOException {

        return null;
    }

    @Override
    public void initiallyFillFromInternal(ObjectInputStream is)
            throws IOException, ClassNotFoundException, InterruptedException {

    }

    @Override
    public void serializeForInitialReplicationInternal(ObjectOutputStream objectOutputStream) throws IOException {

    }

    @Override
    public Iterable<OperationExecutionListener<ReplicableSecurityService>> getOperationExecutionListeners() {

        return null;
    }

    @Override
    public void startedReplicatingFrom(ReplicationMasterDescriptor master) {

    }

    @Override
    public void stoppedReplicatingFrom(ReplicationMasterDescriptor master) {

    }

    @Override
    public void addOperationExecutionListener(OperationExecutionListener<ReplicableSecurityService> listener) {

    }

    @Override
    public void removeOperationExecutionListener(OperationExecutionListener<ReplicableSecurityService> listener) {

    }

    @Override
    public void clearReplicaState() throws MalformedURLException, IOException, InterruptedException {

    }

    @Override
    public boolean isCurrentlyFillingFromInitialLoad() {

        return false;
    }

    @Override
    public boolean isCurrentlyApplyingOperationReceivedFromMaster() {

        return false;
    }

    @Override
    public void setCurrentlyFillingFromInitialLoad(boolean b) {

    }

    @Override
    public void setCurrentlyApplyingOperationReceivedFromMaster(boolean b) {

    }

    @Override
    public void setUnsentOperationToMasterSender(OperationsToMasterSendingQueue service) {

    }

    @Override
    public ReplicationMasterDescriptor getMasterDescriptor() {

        return null;
    }

    @Override
    public void addOperationSentToMasterForReplication(
            OperationWithResultWithIdWrapper<ReplicableSecurityService, ?> operationWithResultWithIdWrapper) {

    }

    @Override
    public boolean hasSentOperationToMaster(OperationWithResult<ReplicableSecurityService, ?> operation) {

        return false;
    }

    @Override
    public <S, O extends OperationWithResult<S, ?>, T> void scheduleForSending(
            OperationWithResult<S, T> operationWithResult, OperationsToMasterSender<S, O> sender) {

    }

    @Override
    public Serializable getId() {

        return null;
    }

    @Override
    public SecurityManager getSecurityManager() {

        return null;
    }

    @Override
    public OwnershipAnnotation getOwnership(QualifiedObjectIdentifier idOfOwnedObject) {

        return null;
    }

    @Override
    public OwnershipAnnotation createDefaultOwnershipForNewObject(QualifiedObjectIdentifier idOfNewObject) {

        return null;
    }

    @Override
    public Iterable<AccessControlListAnnotation> getAccessControlLists() {

        return null;
    }

    @Override
    public AccessControlListAnnotation getAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject) {

        return null;
    }

    @Override
    public SecurityService setEmptyAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject) {

        return null;
    }

    @Override
    public SecurityService setEmptyAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject,
            String displayNameOfAccessControlledObject) {

        return null;
    }

    @Override
    public AccessControlList updateAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject,
            Map<UserGroup, Set<String>> permissionMap) {

        return null;
    }

    @Override
    public AccessControlList overrideAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject,
            Map<UserGroup, Set<String>> permissionMap) {

        return null;
    }

    @Override
    public AccessControlList addToAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject,
            UserGroup userGroup, String action) {

        return null;
    }

    @Override
    public AccessControlList removeFromAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject,
            UserGroup group, String action) {

        return null;
    }

    @Override
    public void deleteAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject) {

    }

    @Override
    public Ownership setOwnership(QualifiedObjectIdentifier idOfOwnedObject, User userOwner, UserGroup tenantOwner) {

        return null;
    }

    @Override
    public Ownership setOwnership(QualifiedObjectIdentifier idOfOwnedObject, User userOwner, UserGroup tenantOwner,
            String displayNameOfOwnedObject) {

        return null;
    }

    @Override
    public void deleteOwnership(QualifiedObjectIdentifier idOfOwnedObject) {

    }

    @Override
    public Iterable<UserGroup> getUserGroupList() {

        return null;
    }

    @Override
    public UserGroup getUserGroup(UUID id) {

        return null;
    }

    @Override
    public UserGroup getUserGroupByName(String name) {

        return null;
    }

    @Override
    public Iterable<UserGroup> getUserGroupsOfUser(User user) {

        return null;
    }

    @Override
    public UserGroup createUserGroup(UUID id, String name) throws UserGroupManagementException {

        return null;
    }

    @Override
    public void addUserToUserGroup(UserGroup group, User user) {

    }

    @Override
    public void removeUserFromUserGroup(UserGroup group, User user) {

    }

    @Override
    public void putRoleDefinitionToUserGroup(UserGroup group, RoleDefinition roleDefinition, boolean forAll) {

    }

    @Override
    public void removeRoleDefintionFromUserGroup(UserGroup group, RoleDefinition roleDefinition) {

    }

    @Override
    public void deleteUserGroup(UserGroup userGroup) throws UserGroupManagementException {

    }

    @Override
    public Iterable<User> getUserList() {

        return null;
    }

    @Override
    public User getUserByName(String username) {

        return null;
    }

    @Override
    public User getUserByEmail(String email) {

        return null;
    }

    @Override
    public User getCurrentUser() {

        return null;
    }

    @Override
    public String login(String username, String password) throws UserManagementException {

        return null;
    }

    @Override
    public String getAuthenticationUrl(Credential credential) throws UserManagementException {

        return null;
    }

    @Override
    public User verifySocialUser(Credential credential) throws UserManagementException {

        return null;
    }

    @Override
    public void logout() {

    }

    @Override
    public User createSimpleUser(String username, String email, String password, String fullName, String company,
            Locale locale, String validationBaseURL, UserGroup userOwner)
            throws UserManagementException, MailException, UserGroupManagementException {

        return null;
    }

    @Override
    public void updateSimpleUserPassword(String name, String newPassword) throws UserManagementException {

    }

    @Override
    public void updateSimpleUserEmail(String username, String newEmail, String validationBaseURL)
            throws UserManagementException {

    }

    @Override
    public void updateUserProperties(String username, String fullName, String company, Locale locale)
            throws UserManagementException {

    }

    @Override
    public User createSocialUser(String username, SocialUserAccount socialUserAccount)
            throws UserManagementException, UserGroupManagementException {

        return null;
    }

    @Override
    public void deleteUser(String username) throws UserManagementException {

    }

    @Override
    public RoleDefinition createRoleDefinition(UUID id, String name) {

        return null;
    }

    @Override
    public void deleteRoleDefinition(RoleDefinition roleDefinition) {

    }

    @Override
    public void updateRoleDefinition(RoleDefinition roleDefinitionWithNewProperties) {

    }

    @Override
    public Iterable<RoleDefinition> getRoleDefinitions() {

        return null;
    }

    @Override
    public RoleDefinition getRoleDefinition(UUID idOfRoleDefinition) {

        return null;
    }

    @Override
    public void addRoleForUser(User user, Role role) {

    }

    @Override
    public void addRoleForUser(String username, Role role) {

    }

    @Override
    public void removeRoleFromUser(User user, Role role) {

    }

    @Override
    public void removeRoleFromUser(String username, Role role) {

    }

    @Override
    public Iterable<WildcardPermission> getPermissionsFromUser(String username) throws UserManagementException {

        return null;
    }

    @Override
    public void removePermissionFromUser(String username, WildcardPermission permissionToRemove) {

    }

    @Override
    public void addPermissionForUser(String username, WildcardPermission permissionToAdd) {

    }

    @Override
    public void addSetting(String key, Class<?> clazz) throws UserManagementException {

    }

    @Override
    public boolean setSetting(String key, Object setting) {

        return false;
    }

    @Override
    public <T> T getSetting(String key, Class<T> clazz) {

        return null;
    }

    @Override
    public Map<String, Object> getAllSettings() {

        return null;
    }

    @Override
    public Map<String, Class<?>> getAllSettingTypes() {

        return null;
    }

    @Override
    public void refreshSecurityConfig(ServletContext context) {

    }

    @Override
    public CacheManager getCacheManager() {

        return null;
    }

    @Override
    public void sendMail(String username, String subject, String body) throws MailException {

    }

    @Override
    public boolean checkPassword(String username, String password) throws UserManagementException {

        return false;
    }

    @Override
    public boolean checkPasswordResetSecret(String username, String passwordResetSecret)
            throws UserManagementException {

        return false;
    }

    @Override
    public void resetPassword(String username, String baseURL) throws UserManagementException, MailException {

    }

    @Override
    public boolean validateEmail(String username, String validationSecret) throws UserManagementException {

        return false;
    }

    @Override
    public void setPreference(String username, String key, String value) {

    }

    @Override
    public void setPreferenceObject(String name, String preferenceKey, Object preference) {

    }

    @Override
    public void unsetPreference(String username, String key) {

    }

    @Override
    public String getPreference(String username, String key) {

        return null;
    }

    @Override
    public <T> T getPreferenceObject(String username, String key) {

        return null;
    }

    @Override
    public Map<String, String> getAllPreferences(String username) {

        return null;
    }

    @Override
    public String createAccessToken(String username) {

        return null;
    }

    @Override
    public String getAccessToken(String username) {

        return null;
    }

    @Override
    public String getOrCreateAccessToken(String username) {

        return null;
    }

    @Override
    public User getUserByAccessToken(String accessToken) {

        return null;
    }

    @Override
    public void removeAccessToken(String username) {

    }

    @Override
    public User loginByAccessToken(String accessToken) {

        return null;
    }

    @Override
    public UserGroup getDefaultTenant() {

        return null;
    }

    @Override
    public <T> T setOwnershipCheckPermissionForObjectCreationAndRevertOnError(HasPermissions type,
            TypeRelativeObjectIdentifier typeRelativeObjectIdentifier, String securityDisplayName,
            Callable<T> createActionReturningCreatedObject) {

        return null;
    }

    @Override
    public void setOwnershipCheckPermissionForObjectCreationAndRevertOnError(HasPermissions type,
            TypeRelativeObjectIdentifier typeRelativeObjectIdentifier, String securityDisplayName,
            Action actionToCreateObject) {

    }

    @Override
    public User getAllUser() {

        return null;
    }

    @Override
    public void checkPermissionAndDeleteOwnershipForObjectRemoval(WithQualifiedObjectIdentifier object,
            Action actionToDeleteObject) {

    }

    @Override
    public <T> T checkPermissionAndDeleteOwnershipForObjectRemoval(WithQualifiedObjectIdentifier object,
            Callable<T> actionToDeleteObject) {

        return null;
    }

    @Override
    public void deleteAllDataForRemovedObject(QualifiedObjectIdentifier identifier) {

    }

    @Override
    public <T extends WithQualifiedObjectIdentifier> void filterObjectsWithPermissionForCurrentUser(
            HasPermissions permittedObject, com.sap.sse.security.shared.HasPermissions.Action action,
            Iterable<T> objectsToFilter, Consumer<T> filteredObjectsConsumer) {

    }

    @Override
    public <T extends WithQualifiedObjectIdentifier> void filterObjectsWithPermissionForCurrentUser(
            HasPermissions permittedObject, com.sap.sse.security.shared.HasPermissions.Action[] actions,
            Iterable<T> objectsToFilter, Consumer<T> filteredObjectsConsumer) {

    }

    @Override
    public <T extends WithQualifiedObjectIdentifier> void filterObjectsWithAnyPermissionForCurrentUser(
            HasPermissions permittedObject, com.sap.sse.security.shared.HasPermissions.Action[] actions,
            Iterable<T> objectsToFilter, Consumer<T> filteredObjectsConsumer) {

    }

    @Override
    public <T extends WithQualifiedObjectIdentifier, R> List<R> mapAndFilterByReadPermissionForCurrentUser(
            HasPermissions permittedObject, Iterable<T> objectsToFilter, Function<T, R> filteredObjectsMapper) {

        return null;
    }

    @Override
    public <T extends WithQualifiedObjectIdentifier, R> List<R> mapAndFilterByExplicitPermissionForCurrentUser(
            HasPermissions permittedObject, com.sap.sse.security.shared.HasPermissions.Action[] actions,
            Iterable<T> objectsToFilter, Function<T, R> filteredObjectsMapper) {

        return null;
    }

    @Override
    public <T extends WithQualifiedObjectIdentifier, R> List<R> mapAndFilterByAnyExplicitPermissionForCurrentUser(
            HasPermissions permittedObject, com.sap.sse.security.shared.HasPermissions.Action[] actions,
            Iterable<T> objectsToFilter, Function<T, R> filteredObjectsMapper) {

        return null;
    }

    @Override
    public boolean hasCurrentUserReadPermission(WithQualifiedObjectIdentifier object) {
        return true;
    }

    @Override
    public boolean hasCurrentUserUpdatePermission(WithQualifiedObjectIdentifier object) {

        return false;
    }

    @Override
    public boolean hasCurrentUserDeletePermission(WithQualifiedObjectIdentifier object) {

        return false;
    }

    @Override
    public boolean hasCurrentUserExplicitPermissions(WithQualifiedObjectIdentifier object,
            com.sap.sse.security.shared.HasPermissions.Action... actions) {

        return false;
    }

    @Override
    public boolean hasCurrentUserOneOfExplicitPermissions(WithQualifiedObjectIdentifier object,
            com.sap.sse.security.shared.HasPermissions.Action... actions) {

        return false;
    }

    @Override
    public void checkCurrentUserReadPermission(WithQualifiedObjectIdentifier object) {

    }

    @Override
    public void checkCurrentUserUpdatePermission(WithQualifiedObjectIdentifier object) {

    }

    @Override
    public void checkCurrentUserDeletePermission(WithQualifiedObjectIdentifier object) {

    }

    @Override
    public void checkCurrentUserDeletePermission(QualifiedObjectIdentifier object) {

    }

    @Override
    public void checkCurrentUserExplicitPermissions(WithQualifiedObjectIdentifier object,
            com.sap.sse.security.shared.HasPermissions.Action... actions) {

    }

    @Override
    public void checkCurrentUserHasOneOfExplicitPermissions(WithQualifiedObjectIdentifier object,
            com.sap.sse.security.shared.HasPermissions.Action... actions) {

    }

    @Override
    public void assumeOwnershipMigrated(String typeName) {

    }

    @Override
    public void migrateOwnership(WithQualifiedObjectIdentifier object) {

    }

    @Override
    public void migrateOwnership(QualifiedObjectIdentifier object, String displayName) {

    }

    @Override
    public void migrateUser(User user) {

    }

    @Override
    public void migratePermission(User user, WildcardPermission permissionToMigrate,
            com.sap.sse.common.Util.Function<WildcardPermission, WildcardPermission> permissionReplacement) {

    }

    @Override
    public void checkMigration(Iterable<HasPermissions> allInstances) {

    }

    @Override
    public <T extends WithQualifiedObjectIdentifier> boolean hasCurrentUserRoleForOwnedObject(HasPermissions type,
            T object, RoleDefinition roleToCheck) {

        return false;
    }

    @Override
    public boolean hasCurrentUserMetaPermission(WildcardPermission permissionToCheck, Ownership ownership) {

        return false;
    }

    @Override
    public void setOwnershipIfNotSet(QualifiedObjectIdentifier identifier, User userOwner, UserGroup defaultTenant) {

    }

    @Override
    public UserGroup getDefaultTenantForCurrentUser() {

        return null;
    }

    @Override
    public boolean hasUserAllWildcardPermissionsForAlreadyRealizedQualifications(RoleDefinition role,
            Iterable<WildcardPermission> permissionsToCheck) {

        return false;
    }

    @Override
    public void setDefaultTenantForCurrentServerForUser(String username, UUID defaultTenantId) {

    }

    @Override
    public void copyUsersAndRoleAssociations(UserGroup source, UserGroup destination, RoleCopyListener callback) {

    }

    @Override
    public User checkPermissionForObjectCreationAndRevertOnErrorForUserCreation(String username,
            Callable<User> createActionReturningCreatedObject) {

        return null;
    }

    @Override
    public <T> T checkPermissionAndDeleteOwnershipForObjectRemoval(QualifiedObjectIdentifier identifier,
            Callable<T> actionToDeleteObject) {

        return null;
    }

    @Override
    public void checkPermissionAndDeleteOwnershipForObjectRemoval(QualifiedObjectIdentifier identifier,
            Action actionToDeleteObject) {

    }

    @Override
    public <T> T doWithTemporaryDefaultTenant(UserGroup tenant, Callable<T> action) {

        return null;
    }

    @Override
    public void initialize() {

    }

    @Override
    public boolean hasCurrentUserMetaPermissionsOfRoleDefinitionWithQualification(RoleDefinition roleDefinition,
            Ownership qualificationForGrantedPermissions) {

        return false;
    }

    @Override
    public boolean isInitialOrMigration() {

        return false;
    }

    @Override
    public RoleDefinition getOrCreateRoleDefinitionFromPrototype(RolePrototype rolePrototype) {

        return null;
    }

    @Override
    public void setDefaultOwnership(QualifiedObjectIdentifier identifier, String description) {

    }

    @Override
    public boolean hasCurrentUserAnyPermission(WildcardPermission permissionToCheck) {

        return false;
    }

    @Override
    public <T> T setOwnershipWithoutCheckPermissionForObjectCreationAndRevertOnError(HasPermissions type,
            TypeRelativeObjectIdentifier typeIdentifier, String securityDisplayName, Callable<T> actionWithResult) {
        return null;
    }

    @Override
    public void setOwnershipWithoutCheckPermissionForObjectCreationAndRevertOnError(HasPermissions type,
            TypeRelativeObjectIdentifier typeRelativeObjectIdentifier, String securityDisplayName,
            Action actionToCreateObject) {
        // TODO Auto-generated method stub

    }

}
