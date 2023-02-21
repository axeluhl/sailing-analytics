package com.sap.sse.security.datamining.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;
import com.sap.sse.security.datamining.data.HasUserGroupContext;
import com.sap.sse.security.datamining.data.HasUserInUserGroupContext;
import com.sap.sse.security.datamining.data.impl.UserInUserGroupWithContext;
import com.sap.sse.security.shared.impl.User;

public class SecurityUserInUserGroupRetrievalProcessor extends AbstractRetrievalProcessor<HasUserGroupContext, HasUserInUserGroupContext> {
    public SecurityUserInUserGroupRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasUserInUserGroupContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(HasUserGroupContext.class, HasUserInUserGroupContext.class, executor, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HasUserInUserGroupContext> retrieveData(HasUserGroupContext element) {
        final Set<HasUserInUserGroupContext> data = new HashSet<>();
        for (final User user : element.getUserGroup().getUsers()) {
            if (isAborted()) {
                break;
            }
            if (element.getSecurityService().hasCurrentUserReadPermission(user)) {
                data.add(new UserInUserGroupWithContext(element, user));
            }
        }
        return data;
    }
}
