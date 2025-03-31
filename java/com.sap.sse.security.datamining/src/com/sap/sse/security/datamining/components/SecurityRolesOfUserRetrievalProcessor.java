package com.sap.sse.security.datamining.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.security.datamining.data.HasRoleOfUserContext;
import com.sap.sse.security.datamining.data.HasUserContext;
import com.sap.sse.security.datamining.data.impl.RoleOfUserWithContext;
import com.sap.sse.security.shared.impl.Role;

public class SecurityRolesOfUserRetrievalProcessor extends AbstractSecurityRolesOfUserRetrievalProcessor<HasUserContext, HasRoleOfUserContext> {
    public SecurityRolesOfUserRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasRoleOfUserContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(executor, HasUserContext.class, HasRoleOfUserContext.class, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected HasRoleOfUserContext createRoleWithContext(HasUserContext element, Role role) {
        return new RoleOfUserWithContext(element, role);
    }
}