package com.sap.sse.security.datamining.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.datamining.data.HasUserGroupContext;
import com.sap.sse.security.datamining.data.impl.UserGroupWithContext;
import com.sap.sse.security.shared.impl.UserGroup;

public class SecurityUserGroupRetrievalProcessor extends AbstractRetrievalProcessor<SecurityService, HasUserGroupContext> {
    public SecurityUserGroupRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasUserGroupContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(SecurityService.class, HasUserGroupContext.class, executor, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HasUserGroupContext> retrieveData(SecurityService element) {
        final Set<HasUserGroupContext> data = new HashSet<>();
        for (final UserGroup userGroup : element.getUserGroupList()) {
            if (isAborted()) {
                break;
            }
            data.add(new UserGroupWithContext(userGroup, element));
        }
        return data;
    }
}
