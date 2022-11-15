package com.sap.sse.security.shared.dto;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.sap.sse.security.shared.impl.AbstractAccessControlList;

public class AccessControlListDTO extends AbstractAccessControlList<StrippedUserGroupDTO> {

    private static final long serialVersionUID = 2878270565599189728L;

    @Deprecated // gwt only
    AccessControlListDTO() {
        super(Collections.<StrippedUserGroupDTO, Set<String>> emptyMap());
    }

    public AccessControlListDTO(Map<StrippedUserGroupDTO, Set<String>> permissionMap) {
        super(permissionMap);
    }

}
