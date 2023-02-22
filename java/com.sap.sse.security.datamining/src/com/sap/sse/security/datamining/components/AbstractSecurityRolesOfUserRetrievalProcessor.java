package com.sap.sse.security.datamining.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;
import com.sap.sse.security.datamining.data.HasRoleContext;
import com.sap.sse.security.datamining.data.HasUserContext;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;
import com.sap.sse.security.shared.impl.PermissionAndRoleAssociation;
import com.sap.sse.security.shared.impl.Role;

public abstract class AbstractSecurityRolesOfUserRetrievalProcessor<HUG extends HasUserContext, HRC extends HasRoleContext>
extends AbstractRetrievalProcessor<HUG, HRC> {
    public AbstractSecurityRolesOfUserRetrievalProcessor(ExecutorService executor, Class<HUG> hasUserContextSpecialization,
            Class<HRC> permissionWithContextSpecialization,
            Collection<Processor<HRC, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(hasUserContextSpecialization, permissionWithContextSpecialization, executor, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HRC> retrieveData(HUG element) {
        final Set<HRC> data = new HashSet<>();
        for (final Role role : element.getUser().getRoles()) {
            if (isAborted()) {
                break;
            }
            final WithQualifiedObjectIdentifier roleAssociation = PermissionAndRoleAssociation.getWithQualifiedObjectIdentifier(role, element.getUser());
            if (element.getSecurityService().hasCurrentUserReadPermission(roleAssociation)) {
                data.add(createRoleWithContext(element, role));
            }
        }
        return data;
    }
    
    protected abstract HRC createRoleWithContext(HUG element, Role role);
}
