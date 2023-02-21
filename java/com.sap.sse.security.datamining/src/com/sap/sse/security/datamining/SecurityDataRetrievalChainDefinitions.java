package com.sap.sse.security.datamining;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.impl.components.SimpleDataRetrieverChainDefinition;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.datamining.components.SecurityPermissionsOfUserInUserGroupRetrievalProcessor;
import com.sap.sse.security.datamining.components.SecurityRoleOfUserGroupRetrievalProcessor;
import com.sap.sse.security.datamining.components.SecurityUserGroupRetrievalProcessor;
import com.sap.sse.security.datamining.components.SecurityUserInUserGroupRetrievalProcessor;
import com.sap.sse.security.datamining.components.SecurityUserRetrievalProcessor;
import com.sap.sse.security.datamining.data.HasPermissionOfUserInUserGroupContext;
import com.sap.sse.security.datamining.data.HasRoleOfUserGroupContext;
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
        userGroupRetriever.startAndEndWith(SecurityUserGroupRetrievalProcessor.class, HasUserGroupContext.class,
                "UserGroups");
        dataRetrieverChainDefinitions.add(userGroupRetriever);
        DataRetrieverChainDefinition<SecurityService, HasUserContext> userRetriever = new SimpleDataRetrieverChainDefinition<>(
                SecurityService.class, HasUserContext.class, "SecurityChainForUsers");
        userRetriever.startAndEndWith(SecurityUserRetrievalProcessor.class, HasUserContext.class,
                "Users");
        dataRetrieverChainDefinitions.add(userRetriever);
        DataRetrieverChainDefinition<SecurityService, HasUserInUserGroupContext> userInUserGroupRetriever = new SimpleDataRetrieverChainDefinition<>(
                userGroupRetriever, HasUserInUserGroupContext.class, "UsersInUserGroup");
        userInUserGroupRetriever.endWith(SecurityUserGroupRetrievalProcessor.class,
                SecurityUserInUserGroupRetrievalProcessor.class, HasUserInUserGroupContext.class, "UsersInUserGroup");
        dataRetrieverChainDefinitions.add(userInUserGroupRetriever);
        DataRetrieverChainDefinition<SecurityService, HasPermissionOfUserInUserGroupContext> permissionsOfUserInUserGroupRetriever = new SimpleDataRetrieverChainDefinition<>(
                userInUserGroupRetriever, HasPermissionOfUserInUserGroupContext.class, "PermissionsOfUsersInUserGroup");
        permissionsOfUserInUserGroupRetriever.endWith(SecurityUserInUserGroupRetrievalProcessor.class,
                SecurityPermissionsOfUserInUserGroupRetrievalProcessor.class,
                HasPermissionOfUserInUserGroupContext.class, "PermissionsOfUsersInUserGroup");
        dataRetrieverChainDefinitions.add(permissionsOfUserInUserGroupRetriever);
        DataRetrieverChainDefinition<SecurityService, HasRoleOfUserGroupContext> roleOfUserGroupRetriever = new SimpleDataRetrieverChainDefinition<>(
                userGroupRetriever, HasRoleOfUserGroupContext.class, "RolesOfUserGroup");
        roleOfUserGroupRetriever.endWith(SecurityUserGroupRetrievalProcessor.class,
                SecurityRoleOfUserGroupRetrievalProcessor.class, HasRoleOfUserGroupContext.class, "RolesOfUserGroup");
        dataRetrieverChainDefinitions.add(roleOfUserGroupRetriever);
    }

    public Iterable<DataRetrieverChainDefinition<?, ?>> getDataRetrieverChainDefinitions() {
        return dataRetrieverChainDefinitions;
    }
}
