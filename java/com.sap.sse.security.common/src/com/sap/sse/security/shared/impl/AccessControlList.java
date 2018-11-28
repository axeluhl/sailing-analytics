package com.sap.sse.security.shared.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;

public class AccessControlList extends AbstractAccessControlList<UserGroup, User> {
    private static final long serialVersionUID = 1L;

    public AccessControlList(Map<UserGroup, Set<String>> permissionMap) {
        super(permissionMap);
    }

    public AccessControlList() {
        super(new HashMap<UserGroup, Set<String>>());
    }

}
