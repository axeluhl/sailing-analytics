package com.sap.sse.security.datamining;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.impl.components.SimpleDataRetrieverChainDefinition;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.datamining.components.SecurityPermissionsOfUserInUserGroupRetrievalProcessor;
import com.sap.sse.security.datamining.components.SecurityPermissionsOfUserRetrievalProcessor;
import com.sap.sse.security.datamining.components.SecurityPreferencesOfUserInUserGroupRetrievalProcessor;
import com.sap.sse.security.datamining.components.SecurityPreferencesOfUserRetrievalProcessor;
import com.sap.sse.security.datamining.components.SecurityRolesOfUserGroupRetrievalProcessor;
import com.sap.sse.security.datamining.components.SecurityRolesOfUserInUserGroupsRetrievalProcessor;
import com.sap.sse.security.datamining.components.SecurityRolesOfUserRetrievalProcessor;
import com.sap.sse.security.datamining.components.SecuritySessionsRetrievalProcessor;
import com.sap.sse.security.datamining.components.SecuritySubscriptionsOfUserRetrievalProcessor;
import com.sap.sse.security.datamining.components.SecurityUserGroupsRetrievalProcessor;
import com.sap.sse.security.datamining.components.SecurityUsersInUserGroupRetrievalProcessor;
import com.sap.sse.security.datamining.components.SecurityUsersRetrievalProcessor;
import com.sap.sse.security.datamining.data.HasPermissionOfUserContext;
import com.sap.sse.security.datamining.data.HasPermissionOfUserInUserGroupContext;
import com.sap.sse.security.datamining.data.HasPreferenceOfUserContext;
import com.sap.sse.security.datamining.data.HasPreferenceOfUserInUserGroupContext;
import com.sap.sse.security.datamining.data.HasRoleOfUserContext;
import com.sap.sse.security.datamining.data.HasRoleOfUserGroupContext;
import com.sap.sse.security.datamining.data.HasRoleOfUserInUserGroupContext;
import com.sap.sse.security.datamining.data.HasSessionContext;
import com.sap.sse.security.datamining.data.HasSubscriptionContext;
import com.sap.sse.security.datamining.data.HasUserContext;
import com.sap.sse.security.datamining.data.HasUserGroupContext;
import com.sap.sse.security.datamining.data.HasUserInUserGroupContext;

/**
 * Builds the security-related retriever chains.
 * <p>
 * 
 * @author D043530 (Axel Uhl)
 *
 */
public class SecurityDataRetrievalChainDefinitions {

    private final Collection<DataRetrieverChainDefinition<?, ?>> dataRetrieverChainDefinitions;

    public SecurityDataRetrievalChainDefinitions() {
        dataRetrieverChainDefinitions = new ArrayList<>();
        DataRetrieverChainDefinition<SecurityService, HasUserGroupContext> userGroupRetriever = new SimpleDataRetrieverChainDefinition<>(
                SecurityService.class, HasUserGroupContext.class, "SecurityChainForUserGroups");
        userGroupRetriever.startAndEndWith(SecurityUserGroupsRetrievalProcessor.class, HasUserGroupContext.class,
                "UserGroups");
        dataRetrieverChainDefinitions.add(userGroupRetriever);
        DataRetrieverChainDefinition<SecurityService, HasUserContext> userRetriever = new SimpleDataRetrieverChainDefinition<>(
                SecurityService.class, HasUserContext.class, "SecurityChainForUsers");
        userRetriever.startAndEndWith(SecurityUsersRetrievalProcessor.class, HasUserContext.class, "Users");
        dataRetrieverChainDefinitions.add(userRetriever);
        DataRetrieverChainDefinition<SecurityService, HasRoleOfUserContext> roleOfUserRetriever = new SimpleDataRetrieverChainDefinition<>(
                userRetriever, HasRoleOfUserContext.class, "RolesOfUsers");
        roleOfUserRetriever.endWith(SecurityUsersRetrievalProcessor.class,
                SecurityRolesOfUserRetrievalProcessor.class, HasRoleOfUserContext.class, "RolesOfUsers");
        dataRetrieverChainDefinitions.add(roleOfUserRetriever);
        DataRetrieverChainDefinition<SecurityService, HasPermissionOfUserContext> permissionOfUserRetriever = new SimpleDataRetrieverChainDefinition<>(
                userRetriever, HasPermissionOfUserContext.class, "PermissionsOfUsers");
        permissionOfUserRetriever.endWith(SecurityUsersRetrievalProcessor.class,
                SecurityPermissionsOfUserRetrievalProcessor.class, HasPermissionOfUserContext.class, "PermissionsOfUsers");
        dataRetrieverChainDefinitions.add(permissionOfUserRetriever);
        DataRetrieverChainDefinition<SecurityService, HasPreferenceOfUserContext> preferenceOfUserRetriever = new SimpleDataRetrieverChainDefinition<>(
                userRetriever, HasPreferenceOfUserContext.class, "PreferenceOfUsers");
        preferenceOfUserRetriever.endWith(SecurityUsersRetrievalProcessor.class,
                SecurityPreferencesOfUserRetrievalProcessor.class, HasPreferenceOfUserContext.class, "PreferenceOfUsers");
        dataRetrieverChainDefinitions.add(preferenceOfUserRetriever);
        DataRetrieverChainDefinition<SecurityService, HasUserInUserGroupContext> userInUserGroupRetriever = new SimpleDataRetrieverChainDefinition<>(
                userGroupRetriever, HasUserInUserGroupContext.class, "UsersInUserGroup");
        userInUserGroupRetriever.endWith(SecurityUserGroupsRetrievalProcessor.class,
                SecurityUsersInUserGroupRetrievalProcessor.class, HasUserInUserGroupContext.class, "UsersInUserGroup");
        dataRetrieverChainDefinitions.add(userInUserGroupRetriever);
        DataRetrieverChainDefinition<SecurityService, HasRoleOfUserInUserGroupContext> roleOfUserInUserGroupRetriever = new SimpleDataRetrieverChainDefinition<>(
                userInUserGroupRetriever, HasRoleOfUserInUserGroupContext.class, "RolesOfUsersInUserGroups");
        roleOfUserInUserGroupRetriever.endWith(SecurityUsersInUserGroupRetrievalProcessor.class,
                SecurityRolesOfUserInUserGroupsRetrievalProcessor.class, HasRoleOfUserInUserGroupContext.class, "RolesOfUsersInUserGroups");
        dataRetrieverChainDefinitions.add(roleOfUserInUserGroupRetriever);
        DataRetrieverChainDefinition<SecurityService, HasPermissionOfUserInUserGroupContext> permissionsOfUserInUserGroupRetriever = new SimpleDataRetrieverChainDefinition<>(
                userInUserGroupRetriever, HasPermissionOfUserInUserGroupContext.class, "PermissionsOfUsersInUserGroup");
        permissionsOfUserInUserGroupRetriever.endWith(SecurityUsersInUserGroupRetrievalProcessor.class,
                SecurityPermissionsOfUserInUserGroupRetrievalProcessor.class,
                HasPermissionOfUserInUserGroupContext.class, "PermissionsOfUsersInUserGroup");
        dataRetrieverChainDefinitions.add(permissionsOfUserInUserGroupRetriever);
        DataRetrieverChainDefinition<SecurityService, HasPreferenceOfUserInUserGroupContext> preferencesOfUserInUserGroupRetriever = new SimpleDataRetrieverChainDefinition<>(
                userInUserGroupRetriever, HasPreferenceOfUserInUserGroupContext.class, "PreferencesOfUsersInUserGroup");
        preferencesOfUserInUserGroupRetriever.endWith(SecurityUsersInUserGroupRetrievalProcessor.class,
                SecurityPreferencesOfUserInUserGroupRetrievalProcessor.class,
                HasPreferenceOfUserInUserGroupContext.class, "PreferencesOfUsersInUserGroup");
        dataRetrieverChainDefinitions.add(preferencesOfUserInUserGroupRetriever);
        DataRetrieverChainDefinition<SecurityService, HasRoleOfUserGroupContext> roleOfUserGroupRetriever = new SimpleDataRetrieverChainDefinition<>(
                userGroupRetriever, HasRoleOfUserGroupContext.class, "RolesOfUserGroup");
        roleOfUserGroupRetriever.endWith(SecurityUserGroupsRetrievalProcessor.class,
                SecurityRolesOfUserGroupRetrievalProcessor.class, HasRoleOfUserGroupContext.class, "RolesOfUserGroup");
        dataRetrieverChainDefinitions.add(roleOfUserGroupRetriever);
        DataRetrieverChainDefinition<SecurityService, HasSessionContext> sessionRetriever = new SimpleDataRetrieverChainDefinition<>(
                SecurityService.class, HasSessionContext.class, "Sessions");
        sessionRetriever.startAndEndWith(SecuritySessionsRetrievalProcessor.class, HasSessionContext.class, "Sessions");
        dataRetrieverChainDefinitions.add(sessionRetriever);
        DataRetrieverChainDefinition<SecurityService, HasSubscriptionContext> subscriptionRetriever = new SimpleDataRetrieverChainDefinition<>(
                userRetriever, HasSubscriptionContext.class, "Subscriptions");
        subscriptionRetriever.endWith(SecurityUsersRetrievalProcessor.class,
                SecuritySubscriptionsOfUserRetrievalProcessor.class, HasSubscriptionContext.class, "Subscriptions");
        dataRetrieverChainDefinitions.add(subscriptionRetriever);
    }

    public Iterable<DataRetrieverChainDefinition<?, ?>> getDataRetrieverChainDefinitions() {
        return dataRetrieverChainDefinitions;
    }
}
