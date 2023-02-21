package com.sap.sse.security.datamining.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;
import com.sap.sse.security.datamining.data.HasRoleOfUserGroupContext;
import com.sap.sse.security.datamining.data.HasUserGroupContext;
import com.sap.sse.security.datamining.data.impl.RoleOfUserGroupWithContext;
import com.sap.sse.security.shared.RoleDefinition;

public class SecurityRoleOfUserGroupRetrievalProcessor extends AbstractRetrievalProcessor<HasUserGroupContext, HasRoleOfUserGroupContext> {
    public SecurityRoleOfUserGroupRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasRoleOfUserGroupContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(HasUserGroupContext.class, HasRoleOfUserGroupContext.class, executor, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HasRoleOfUserGroupContext> retrieveData(HasUserGroupContext element) {
        final Set<HasRoleOfUserGroupContext> data = new HashSet<>();
        for (final Entry<RoleDefinition, Boolean> roleDefinitionAndForAll : element.getUserGroup().getRoleDefinitionMap().entrySet()) {
            if (isAborted()) {
                break;
            }
            data.add(new RoleOfUserGroupWithContext(element, roleDefinitionAndForAll.getKey(), roleDefinitionAndForAll.getValue()));
        }
        return data;
    }
}
