package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;

import com.sap.sse.security.shared.UserGroup;

public class MigrateGroupOwnerForHierarchyDTO implements Serializable {

    private static final long serialVersionUID = -8300235710577973351L;
    
    private UserGroup existingUserGroup;
    private boolean createNewGroup;
    private String newGroupName;
    private boolean updateCompetitors;
    private boolean updateBoats;

    @Deprecated // for GWT serialization only
    protected MigrateGroupOwnerForHierarchyDTO() {
    }

    public MigrateGroupOwnerForHierarchyDTO(UserGroup existingUserGroup, boolean createNewGroup, String newGroupName,
            boolean updateCompetitors, boolean updateBoats) {
        this.existingUserGroup = existingUserGroup;
        this.createNewGroup = createNewGroup;
        this.newGroupName = newGroupName;
        this.updateCompetitors = updateCompetitors;
        this.updateBoats = updateBoats;
    }

    public UserGroup getExistingUserGroup() {
        return existingUserGroup;
    }

    public boolean isCreateNewGroup() {
        return createNewGroup;
    }

    public String getGroupName() {
        return newGroupName;
    }

    public boolean isUpdateCompetitors() {
        return updateCompetitors;
    }

    public boolean isUpdateBoats() {
        return updateBoats;
    }
}
