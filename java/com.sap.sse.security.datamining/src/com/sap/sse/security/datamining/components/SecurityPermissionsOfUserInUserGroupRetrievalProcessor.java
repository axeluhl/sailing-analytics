package com.sap.sse.security.datamining.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.security.datamining.data.HasPermissionOfUserInUserGroupContext;
import com.sap.sse.security.datamining.data.HasUserInUserGroupContext;
import com.sap.sse.security.datamining.data.impl.PermissionOfUserInUserGroupWithContext;
import com.sap.sse.security.shared.WildcardPermission;

public class SecurityPermissionsOfUserInUserGroupRetrievalProcessor extends AbstractSecurityPermissionsOfUserRetrievalProcessor<HasUserInUserGroupContext, HasPermissionOfUserInUserGroupContext> {
    public SecurityPermissionsOfUserInUserGroupRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasPermissionOfUserInUserGroupContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(executor, HasUserInUserGroupContext.class, HasPermissionOfUserInUserGroupContext.class, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected HasPermissionOfUserInUserGroupContext createPermissionWithContext(HasUserInUserGroupContext element,
            WildcardPermission permission) {
        return new PermissionOfUserInUserGroupWithContext(element, permission);
    }
}
