package com.sap.sailing.domain.common.security;

import com.sap.sse.security.shared.Role;

public enum Roles implements Role {
    spectator("spectator"),
    moderator("moderator"),
    sailor("sailor"),
    coach("coach"),
    eventmanager("eventmanager"),
    mediaeditor("mediaeditor");
    
    private Roles(String rolename) {
        this.rolename = rolename;
    }
    
    public String getRolename() {
        return rolename;
    }

    private final String rolename;
}
