package com.sap.sse.security.datamining.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;
import com.sap.sse.security.datamining.data.HasUserContext;
import com.sap.sse.security.datamining.data.HasPermissionContext;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;
import com.sap.sse.security.shared.impl.PermissionAndRoleAssociation;

public abstract class AbstractSecurityPermissionsOfUserRetrievalProcessor<HUG extends HasUserContext, PWC extends HasPermissionContext>
extends AbstractRetrievalProcessor<HUG, PWC> {
    public AbstractSecurityPermissionsOfUserRetrievalProcessor(ExecutorService executor, Class<HUG> hasUserContextSpecialization,
            Class<PWC> permissionWithContextSpecialization,
            Collection<Processor<PWC, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(hasUserContextSpecialization, permissionWithContextSpecialization, executor, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<PWC> retrieveData(HUG element) {
        final Set<PWC> data = new HashSet<>();
        for (final WildcardPermission permission : element.getUser().getPermissions()) {
            if (isAborted()) {
                break;
            }
            final WithQualifiedObjectIdentifier permissionAssociation = PermissionAndRoleAssociation.getWithQualifiedObjectIdentifier(permission, element.getUser());
            if (element.getSecurityService().hasCurrentUserReadPermission(permissionAssociation)) {
                data.add(createPermissionWithContext(element, permission));
            }
        }
        return data;
    }
    
    protected abstract PWC createPermissionWithContext(HUG element, WildcardPermission permission);
}
