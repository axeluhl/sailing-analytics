package com.sap.sse.security.shared;

public interface RolePermissionModel {
    Iterable<String> getPermissions(String role);
}
