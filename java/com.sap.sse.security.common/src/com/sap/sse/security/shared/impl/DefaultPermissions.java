package com.sap.sse.security.shared.impl;

import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.WildcardPermission;

public enum DefaultPermissions implements com.sap.sse.security.shared.HasPermissions {
    // back-end permissions
    USER,
    USER_GROUP,
    ;
    
    // TODO once we can use Java8 here, move this up into a "default" method on the Permission interface
    @Override
    public String getStringPermission(com.sap.sse.security.shared.HasPermissions.Action... modes) {
        final String result;
        if (modes==null || modes.length==0) {
            result = name();
        } else {
            final StringBuilder modesString = new StringBuilder();
            boolean first = true;
            for (com.sap.sse.security.shared.HasPermissions.Action mode : modes) {
                if (first) {
                    first = false;
                } else {
                    modesString.append(',');
                }
                modesString.append(mode.getStringPermission());
            }
            result = name()+":"+modesString.toString();
        }
        return result;
    }

    @Override
    public WildcardPermission getPermission(com.sap.sse.security.shared.HasPermissions.Action... modes) {
        return new WildcardPermission(getStringPermission(modes));
    }

    // TODO once we can use Java8 here, move this up into a "default" method on the Permission interface
    @Override
    public String getStringPermissionForObjects(com.sap.sse.security.shared.HasPermissions.Action mode, String... typeRelativeObjectIdentifiers) {
        final WildcardPermissionEncoder permissionEncoder = new WildcardPermissionEncoder();
        final StringBuilder result = new StringBuilder(getStringPermission(mode));
        if (typeRelativeObjectIdentifiers!=null && typeRelativeObjectIdentifiers.length>0) {
            result.append(':');
            boolean first = true;
            for (String typeRelativeObjectIdentifier : typeRelativeObjectIdentifiers) {
                if (first) {
                    first = false;
                } else {
                    result.append(',');
                }
                result.append(permissionEncoder.encodeAsPermissionPart(typeRelativeObjectIdentifier));
            }
        }
        return result.toString();
    }
    
    @Override
    public QualifiedObjectIdentifier getQualifiedObjectIdentifier(String typeRelativeObjectIdentifier) {
        return new QualifiedObjectIdentifierImpl(name(), typeRelativeObjectIdentifier);
    }

    @Override
    public WildcardPermission getPermissionForObjects(com.sap.sse.security.shared.HasPermissions.Action mode, String... objectIdentifiers) {
        return new WildcardPermission(getStringPermissionForObjects(mode, objectIdentifiers));
    }
}
