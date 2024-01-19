package com.sap.sse.security.datamining.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.security.datamining.data.HasPermissionOfUserContext;
import com.sap.sse.security.datamining.data.HasUserContext;
import com.sap.sse.security.datamining.data.impl.PermissionOfUserWithContext;
import com.sap.sse.security.shared.WildcardPermission;

public class SecurityPermissionsOfUserRetrievalProcessor extends AbstractSecurityPermissionsOfUserRetrievalProcessor<HasUserContext, HasPermissionOfUserContext> {
    public SecurityPermissionsOfUserRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasPermissionOfUserContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(executor, HasUserContext.class, HasPermissionOfUserContext.class, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected HasPermissionOfUserContext createPermissionWithContext(HasUserContext element,
            WildcardPermission permission) {
        return new PermissionOfUserWithContext(element, permission);
    }
}
