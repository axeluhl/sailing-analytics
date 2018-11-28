package com.sap.sse.security.shared.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AccessControlList extends AbstractAccessControlList<UserGroup, User> {
    private static final long serialVersionUID = 1L;

    public AccessControlList(Map<UserGroup, Set<String>> permissionMap) {
        super(permissionMap);
    }

    public AccessControlList() {
        super(new HashMap<UserGroup, Set<String>>());
    }

}
