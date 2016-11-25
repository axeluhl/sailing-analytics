package com.sap.sse.security.shared;

public class Role implements AbstractRole {
    private final AbstractRole role;
    private final String tenant;
    private final String instance;
    
    public Role(AbstractRole role, String tenant, String instance) {
        this.role = role;
        this.tenant = tenant;
        this.instance = instance;
    }
    
    @Override
    public String getRolename() {
        return role.getRolename();
    }

    public String getTenant() {
        return tenant;
    }

    public String getInstance() {
        return instance;
    }

    @Override
    public String name() {
        return role.name();
    }

    @Override
    public int ordinal() {
        return role.ordinal();
    }
}
