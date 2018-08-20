package com.sap.sse.security.shared;


/**
 * Roles can imply permissions. For example, an application may define that the role "admin" can do everything by
 * returning a "*" permission from {@link #getPermissions(String)}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface PermissionsForRoleProvider {
    Iterable<String> getPermissions(String role);
}
