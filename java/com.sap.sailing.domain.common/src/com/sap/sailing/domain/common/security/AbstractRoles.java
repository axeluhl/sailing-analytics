package com.sap.sailing.domain.common.security;

import com.sap.sse.security.shared.AbstractRole;

public enum AbstractRoles implements AbstractRole {
    spectator("spectator"),
    moderator("moderator"),
    sailor("sailor"),
    coach("coach"),
    eventmanager("eventmanager"),
    mediaeditor("mediaeditor");
    
    private final String rolename;
    
    private AbstractRoles(String rolename) {
        this.rolename = rolename;
    }
    
    public String getRolename() {
        return rolename;
    }
}
