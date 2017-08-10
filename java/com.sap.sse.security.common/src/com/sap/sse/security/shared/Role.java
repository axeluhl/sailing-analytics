package com.sap.sse.security.shared;

import java.util.ArrayList;
import java.util.List;

public class Role implements AbstractRole {
    private final AbstractRole abstractRole;
    private final String rolename;
    private final String tenant;
    private final String instance;

    public class EmptyTenantOrInstanceException extends Exception {
        private static final long serialVersionUID = -2425368113141277409L;
    }

    /**
     * @param tenant permitted values are "*" or a tenant name
     * @param instance permitted values are "*" or a instance name
     */
    public Role(AbstractRole role, String tenant, String instance) throws EmptyTenantOrInstanceException {
        if (tenant == "" || instance == "") {
            throw new EmptyTenantOrInstanceException();
        }
        this.rolename = role.getRolename() + ":" + tenant + ":" + instance;
        this.abstractRole = role;
        this.tenant = tenant;
        this.instance = instance;
    }

    public Role(String abstractRolename, String tenant, String instance) throws EmptyTenantOrInstanceException {
        this(AbstractRoles.valueOf(abstractRolename), tenant, instance);
    }

    @Override
    public String getRolename() {
        return rolename;
    }

    @Override
    public Iterable<String> getPermissions() {
        List<String> permissions = new ArrayList<>();
        for (String permission : abstractRole.getPermissions()) {
            permissions.add(permission + ":" + tenant + ":" + "instance");
        }
        return permissions;
    }

    public String getTenant() {
        return tenant;
    }

    public String getInstance() {
        return instance;
    }

    @Override
    public String name() {
        return abstractRole.name();
    }

    @Override
    public int ordinal() {
        return abstractRole.ordinal();
    }
}
