package com.sap.sse.security.shared.impl;

import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.WildcardPermission;

public class PermissionImpl implements HasPermissions {
    private final String permissionTypeName;
    
    private PermissionImpl(String permissionTypeName) {
        this.permissionTypeName = permissionTypeName;
    }
    
    public String getStringPermission(Action... modes) {
        final String result;
        if (modes==null || modes.length==0) {
            result = permissionTypeName;
        } else {
            final StringBuilder modesString = new StringBuilder();
            boolean first = true;
            for (Action mode : modes) {
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
    public WildcardPermission getPermission(Action... modes) {
        return new WildcardPermission(getStringPermission(modes));
    }

    public String getStringPermissionForObjects(Action mode, String... objectIdentifiers) {
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
                result.append(permissionEncoder.encodeAsPermissionPart(objectIdentifier));
            }
        }
        return result.toString();
    }
    
    @Override
    public WildcardPermission getPermissionForObjects(Action mode, String... objectIdentifiers) {
        return new WildcardPermission(getStringPermissionForObjects(mode, objectIdentifiers));
    }

    @Override
    public QualifiedObjectIdentifier getQualifiedObjectIdentifier(String typeRelativeObjectIdentifier) {
        return new QualifiedObjectIdentifierImpl(name(), typeRelativeObjectIdentifier);
    }

    @Override
    public String name() {
        return permissionTypeName;
    }
}

