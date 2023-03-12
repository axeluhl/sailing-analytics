package com.sap.sse.security.datamining.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.security.datamining.data.HasRoleOfUserInUserGroupContext;
import com.sap.sse.security.datamining.data.HasUserInUserGroupContext;
import com.sap.sse.security.datamining.data.impl.RoleOfUserInUserGroupWithContext;
import com.sap.sse.security.shared.impl.Role;

public class SecurityRolesOfUserInUserGroupsRetrievalProcessor
extends AbstractSecurityRolesOfUserRetrievalProcessor<HasUserInUserGroupContext, HasRoleOfUserInUserGroupContext> {
    public SecurityRolesOfUserInUserGroupsRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasRoleOfUserInUserGroupContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(executor, HasUserInUserGroupContext.class, HasRoleOfUserInUserGroupContext.class, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected HasRoleOfUserInUserGroupContext createRoleWithContext(HasUserInUserGroupContext element, Role role) {
        return new RoleOfUserInUserGroupWithContext(element, role);
    }
}