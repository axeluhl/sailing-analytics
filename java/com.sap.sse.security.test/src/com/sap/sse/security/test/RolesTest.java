package com.sap.sse.security.test;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;

import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.RoleDefinitionImpl;
import com.sap.sse.security.shared.RoleImpl;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.impl.SecurityUserImpl;
import com.sap.sse.security.shared.impl.UserGroupImpl;

public class RolesTest {
    @Test
    public void testRoleToString() {
        final RoleDefinition roleDefinition = new RoleDefinitionImpl(UUID.randomUUID(), "role");
        final Role role = new RoleImpl(roleDefinition);
        final UserGroup tenant = new UserGroupImpl(UUID.randomUUID(), "tenant");
        final SecurityUserImpl user = new SecurityUserImpl("user");
        assertEquals("role", role.toString());
        final Role role2 = new RoleImpl(roleDefinition, tenant, /* user qualification */ null);
        assertEquals("role:tenant", role2.toString());
        final Role role3 = new RoleImpl(roleDefinition, tenant, user);
        assertEquals("role:tenant:user", role3.toString());
        final Role role4 = new RoleImpl(roleDefinition, /* tenant qualification */ null, user);
        assertEquals("role::user", role4.toString());
    }
}
