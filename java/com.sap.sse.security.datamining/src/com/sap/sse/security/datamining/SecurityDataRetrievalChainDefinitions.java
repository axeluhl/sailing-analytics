package com.sap.sse.security.datamining;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.impl.components.SimpleDataRetrieverChainDefinition;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.datamining.components.SecurityRoleOfUserGroupRetrievalProcessor;
import com.sap.sse.security.datamining.components.SecurityUserGroupRetrievalProcessor;
import com.sap.sse.security.datamining.components.SecurityUserInUserGroupRetrievalProcessor;
import com.sap.sse.security.datamining.data.HasRoleOfUserGroupContext;
import com.sap.sse.security.datamining.data.HasUserGroupContext;
import com.sap.sse.security.datamining.data.HasUserInUserGroupContext;

/**
 * Builds the security-related retriever chains.<p>
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
                "UserGroup");
        dataRetrieverChainDefinitions.add(userGroupRetriever);
        DataRetrieverChainDefinition<SecurityService, HasUserInUserGroupContext> userInUserGroupRetriever = new SimpleDataRetrieverChainDefinition<>(
                userGroupRetriever, HasUserInUserGroupContext.class, "UserInUserGroup");
        userInUserGroupRetriever.endWith(SecurityUserGroupRetrievalProcessor.class, SecurityUserInUserGroupRetrievalProcessor.class, HasUserInUserGroupContext.class, "UserInUserGroup");
        dataRetrieverChainDefinitions.add(userInUserGroupRetriever);
        DataRetrieverChainDefinition<SecurityService, HasRoleOfUserGroupContext> roleOfUserGroupRetriever = new SimpleDataRetrieverChainDefinition<>(
                userGroupRetriever, HasRoleOfUserGroupContext.class, "RoleOfUserGroup");
        roleOfUserGroupRetriever.endWith(SecurityUserGroupRetrievalProcessor.class, SecurityRoleOfUserGroupRetrievalProcessor.class, HasRoleOfUserGroupContext.class, "RoleInUserGroup");
        dataRetrieverChainDefinitions.add(roleOfUserGroupRetriever);
    }

    public Iterable<DataRetrieverChainDefinition<?, ?>> getDataRetrieverChainDefinitions() {
        return dataRetrieverChainDefinitions;
    }
}
