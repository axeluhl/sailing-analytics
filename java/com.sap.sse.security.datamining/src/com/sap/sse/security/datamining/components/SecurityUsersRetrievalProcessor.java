package com.sap.sse.security.datamining.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.datamining.data.HasUserContext;
import com.sap.sse.security.datamining.data.impl.UserWithContext;
import com.sap.sse.security.shared.impl.User;

public class SecurityUsersRetrievalProcessor extends AbstractRetrievalProcessor<SecurityService, HasUserContext> {
    public SecurityUsersRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasUserContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(SecurityService.class, HasUserContext.class, executor, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HasUserContext> retrieveData(SecurityService securityService) {
        final Set<HasUserContext> data = new HashSet<>();
        for (final User user : securityService.getUserList()) {
            if (isAborted()) {
                break;
            }
            if (securityService.hasCurrentUserReadPermission(user)) {
                data.add(new UserWithContext(user, securityService));
            }
        }
        return data;
    }
}
