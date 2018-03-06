package com.sap.sse.security.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.authz.Permission;

import com.sap.sse.security.AccessControlStore;
import com.sap.sse.security.shared.WildcardPermission;

/**
 * Can convert Shiro {@link Permission} objects into {@link WildcardPermission} objects and can extract an object ID as
 * {@link String} from either such permission which can then be used to parameterize the {@link AccessControlStore},
 * e.g., to look up ownership and ACL information for an object for which permissions are requested.
 * <p>
 * 
 * Note that the Shiro permissions are all lowercase. All parts of a
 * {@link org.apache.shiro.authz.permission.WildcardPermission} will always be mapped to lowercase before any further
 * processing. Keep this in mind when passing object IDs through permission parts.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class PermissionConverter {
    public WildcardPermission getWildcardPermission(Permission permission) {
        return new WildcardPermission(getAsString(permission));
    }

    private String getAsString(Permission permission) {
        return permission.toString().replaceAll("\\[|\\]", "");
    }
    
    /**
     * Splits the permission along ":" occurrences
     */
    public String[] getPermissionParts(Permission permission) {
        return getAsString(permission).split(":");
    }
    
    /**
     * From the permission type (first element) and the comma-separated list of relative object IDs in the third element
     * assembles IDs as strings that can be used, e.g., to look up ACLs or ownerships for the object for which the
     * {@code permission} is requested.
     */
    public Iterable<String> getObjectIdsAsString(Permission permission) {
        final String[] parts = getPermissionParts(permission);
        final List<String> result = new ArrayList<>();
        if (parts.length > 2) {
            boolean first = true;
            for (final String relativeId : parts[2].split(",")) {
                final String trimmedRelativeId;
                if (first) {
                    first = false;
                    trimmedRelativeId = relativeId;
                } else {
                    // Shiro wildcard permissions with multiple elements in a part will introduce leading
                    // spaces for each element after the first, with the space following the comma:
                    trimmedRelativeId = relativeId.substring(1);
                }
                result.add(getIdFromPermissionTypeAndRelativeObjectId(parts[0], trimmedRelativeId));
            }
        }
        return result;
    }
    
    /**
     * Connects the two parts by a slash "/" character after escaping all slashes in either of the two
     * strings by preceding it with a backslash
     */
    private String getIdFromPermissionTypeAndRelativeObjectId(String permissionTypeName, String relativeObjectId) {
        return permissionTypeName.replaceAll("/", "\\\\/")+"/"+relativeObjectId.replaceAll("/", "\\\\/");
    }
}
