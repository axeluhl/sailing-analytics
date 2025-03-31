package com.sap.sailing.server.testsupport;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.common.Util;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissionsProvider;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;

/*
 * Mocks a HasPermissionProvider that contains the most used HasPermissionTypes.
 */
public class MockedHasPermissionProvider implements HasPermissionsProvider{
    @Override
    public Iterable<HasPermissions> getAllHasPermissions() {
        Set<HasPermissions> result = new HashSet<>();
        Util.addAll(SecuredSecurityTypes.getAllInstances(), result);
        Util.addAll(SecuredDomainType.getAllInstances(), result);
        return result;
    }
}