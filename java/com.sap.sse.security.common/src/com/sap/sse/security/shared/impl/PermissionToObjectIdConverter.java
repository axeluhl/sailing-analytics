package com.sap.sse.security.shared.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.WildcardPermission;

/**
 * Can convert Shiro {@link HasPermissions} objects into {@link WildcardPermission} objects and can extract an object ID as
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
public class PermissionToObjectIdConverter {
    /**
     * From the permission type (first element) and the comma-separated list of relative object IDs in the third element
     * assembles IDs as strings that can be used, e.g., to look up ACLs or ownerships for the object for which the
     * {@code permission} is requested.
     */
    public Iterable<String> getObjectIdsAsString(WildcardPermission permission) {
        final List<Set<String>> parts = permission.getParts();
        final List<String> result = new ArrayList<>();
        if (parts.size() > 2) {
            for (final String relativeId : parts.get(2)) {
                result.add(getIdFromPermissionTypeAndRelativeObjectId(parts.get(0).iterator().next(), relativeId));
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
