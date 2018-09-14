package com.sap.sse.security.shared;

import com.sap.sse.common.WithID;

public class PermissionBuilderImpl implements PermissionBuilder<WildcardPermission> {
    private static PermissionBuilderImpl instance;
    
    public static PermissionBuilderImpl getInstance() {
        if (instance == null) {
            instance = new PermissionBuilderImpl();
        }
        return instance;
    }
    
    @Override
    public WildcardPermission getPermission(String objectType, Action action) {
        return new WildcardPermission(objectType + ":" + action.name());
    }
    
    @Override
    public WildcardPermission getPermission(Class<?> objectType, Action action) {
        return getPermission(objectType.getName(), action);
    }
    
    @Override
    public WildcardPermission getPermission(String objectType, Action action, String objectId) {
        return new WildcardPermission(objectType + ":" + action.name() + ":" + objectId);
    }
    
    @Override
    public WildcardPermission getPermission(Class<?> objectType, Action action, String objectId) {
        return getPermission(objectType.getName(), action, objectId);
    }
    
    @Override
    public WildcardPermission getPermission(Class<?> objectType, Action action, WithID object) {
        return getPermission(objectType.getName(), action, object.getId().toString());
    }
    
    @Override
    public WildcardPermission getPermission(WithID object, Action action) {
        return getPermission(object.getClass(), action, object);
    }
}
