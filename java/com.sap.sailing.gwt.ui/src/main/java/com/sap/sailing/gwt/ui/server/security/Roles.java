package com.sap.sailing.gwt.ui.server.security;

import com.sap.sse.security.shared.Role;

public enum Roles implements Role {
    EVENT_MANAGER("event_manager");

    private Roles(String rolename) {
        this.rolename = rolename;
    }
    
    @Override
    public String getRolename() {
        return rolename;
    }
    
    private final String rolename;
}
