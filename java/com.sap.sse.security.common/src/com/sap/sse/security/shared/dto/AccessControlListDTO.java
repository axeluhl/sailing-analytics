package com.sap.sse.security.shared.dto;

import java.util.Map;
import java.util.Set;

import com.sap.sse.security.shared.impl.AbstractAccessControlList;

public class AccessControlListDTO extends AbstractAccessControlList<UserGroupDTO, StrippedUserDTO> {
    private static final long serialVersionUID = 1L;

    public AccessControlListDTO(Map<UserGroupDTO, Set<String>> permissionMap) {
        super(permissionMap);
    }

}
