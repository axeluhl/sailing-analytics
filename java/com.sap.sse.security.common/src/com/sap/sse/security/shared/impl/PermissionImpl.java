package com.sap.sse.security.shared.impl;

import com.sap.sse.security.shared.Permission;
import com.sap.sse.security.shared.WildcardPermission;

public class PermissionImpl implements Permission {
    private final String permissionTypeName;
    
    private PermissionImpl(String permissionTypeName) {
        this.permissionTypeName = permissionTypeName;
    }
    
    public String getStringPermission(Mode... modes) {
        final String result;
        if (modes==null || modes.length==0) {
            result = permissionTypeName;
        } else {
            final StringBuilder modesString = new StringBuilder();
            boolean first = true;
            for (Mode mode : modes) {
                if (first) {
                    first = false;
                } else {
                    modesString.append(',');
                }
                modesString.append(mode.getStringPermission());
            }
            result = permissionTypeName+":"+modesString.toString();
        }
        return result;
    }
    
    @Override
    public WildcardPermission getPermission(Mode... modes) {
        return new WildcardPermission(getStringPermission(modes), /* case sensitive */ true);
    }

    public String getStringPermissionForObjects(Mode mode, String... objectIdentifiers) {
        final WildcardPermissionEncoder permissionEncoder = new WildcardPermissionEncoder();
        final StringBuilder result = new StringBuilder(getStringPermission(mode));
        if (objectIdentifiers!=null && objectIdentifiers.length>0) {
            result.append(':');
            boolean first = true;
            for (String objectIdentifier : objectIdentifiers) {
                if (first) {
                    first = false;
                } else {
                    result.append(',');
                }
                result.append(permissionEncoder.encodeAsPermissionPart(getQualifiedObjectIdentifier(objectIdentifier)));
            }
        }
        return result.toString();
    }
    
    @Override
    public WildcardPermission getPermissionForObjects(Mode mode, String... objectIdentifiers) {
        return new WildcardPermission(getStringPermissionForObjects(mode, objectIdentifiers), /* case sensitive */ true);
    }

    @Override
    public String getQualifiedObjectIdentifier(String objectIdentifier) {
        return name()+QUALIFIER_SEPARATOR+objectIdentifier;
    }

    @Override
    public String name() {
        return permissionTypeName;
    }
}

