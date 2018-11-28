package com.sap.sse.security.test;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;

import com.sap.sse.security.shared.dto.RoleDTO;
import com.sap.sse.security.shared.dto.RoleDefinitionDTO;
import com.sap.sse.security.shared.dto.StrippedUserDTO;
import com.sap.sse.security.shared.dto.UserGroupDTO;
import com.sap.sse.security.shared.impl.Role;
import com.sap.sse.security.shared.impl.UserGroup;

public class RolesTest {
    @Test
    public void testRoleToString() {
        final RoleDefinitionDTO roleDefinition = new RoleDefinitionDTO(UUID.randomUUID(), "role");
        final Role role = new Role(roleDefinition);
        final UserGroup tenant = new UserGroup(UUID.randomUUID(), "tenant");
        final UserGroupDTO tenantDTO = new UserGroupDTO(UUID.randomUUID(), "tenant");
        final StrippedUserDTO user = new StrippedUserDTO("user");
        assertEquals("role", role.toString());
        final Role role2 = new Role(roleDefinition, tenant, /* user qualification */ null);
        assertEquals("role:tenant", role2.toString());
        final RoleDTO role3 = new RoleDTO(roleDefinition, tenantDTO, user);
        assertEquals("role:tenant:user", role3.toString());
        final RoleDTO role4 = new RoleDTO(roleDefinition, /* tenant qualification */ null, user);
        assertEquals("role::user", role4.toString());
    }
}
