package com.sap.sailing.gwt.ui.usermanagement;

import com.sap.sse.security.shared.DefaultRoles;

public enum UserRoles {
    administrator(DefaultRoles.ADMIN.getRolename()),
    spectator("spectator"),
    moderator("moderator"),
    sailor("sailor"),
    coach("coach"),
    eventmanager("eventmanager"),
    mediaeditor("mediaeditor");
    
    private UserRoles(String rolename) {
        this.rolename = rolename;
    }
    
    public String getRolename() {
        return rolename;
    }

    private final String rolename;
}
