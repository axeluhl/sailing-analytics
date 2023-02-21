package com.sap.sse.security.datamining.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;
import com.sap.sse.security.datamining.data.HasPermissionsOfUserInUserGroupContext;
import com.sap.sse.security.datamining.data.HasUserInUserGroupContext;
import com.sap.sse.security.datamining.data.impl.PermissionOfUserInUserGroupWithContext;
import com.sap.sse.security.shared.WildcardPermission;

public class SecurityPermissionsOfUserInUserGroupRetrievalProcessor extends AbstractRetrievalProcessor<HasUserInUserGroupContext, HasPermissionsOfUserInUserGroupContext> {
    public SecurityPermissionsOfUserInUserGroupRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasPermissionsOfUserInUserGroupContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(HasUserInUserGroupContext.class, HasPermissionsOfUserInUserGroupContext.class, executor, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HasPermissionsOfUserInUserGroupContext> retrieveData(HasUserInUserGroupContext element) {
        final Set<HasPermissionsOfUserInUserGroupContext> data = new HashSet<>();
        for (final WildcardPermission permission : element.getUser().getPermissions()) {
            if (isAborted()) {
                break;
            }
            data.add(new PermissionOfUserInUserGroupWithContext(element, permission));
        }
        return data;
    }
}
