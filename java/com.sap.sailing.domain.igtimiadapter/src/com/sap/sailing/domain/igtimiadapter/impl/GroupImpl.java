package com.sap.sailing.domain.igtimiadapter.impl;

import com.sap.sailing.domain.igtimiadapter.Group;
import com.sap.sailing.domain.igtimiadapter.Permission;

public class GroupImpl extends HasIdImpl implements Group {
    private final String name;
    private final Iterable<Permission> permissions;
    private final boolean hidden;
    private final boolean blob;
    
    public GroupImpl(long id, String name, Iterable<Permission> permissions, boolean hidden, boolean blob) {
        super(id);
        this.name = name;
        this.permissions = permissions;
        this.hidden = hidden;
        this.blob = blob;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Iterable<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    @Override
    public boolean isBlob() {
        return blob;
    }
}
