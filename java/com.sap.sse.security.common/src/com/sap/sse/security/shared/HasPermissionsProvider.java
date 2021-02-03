package com.sap.sse.security.shared;

@FunctionalInterface
public interface HasPermissionsProvider {
    Iterable<HasPermissions> getAllHasPermissions();
}
