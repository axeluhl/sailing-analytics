package com.sap.sse.security;

import org.apache.shiro.authz.permission.WildcardPermission;

import com.sap.sse.common.WithID;
import com.sap.sse.security.shared.PermissionBuilder;

public class ShiroPermissionBuilderImpl implements PermissionBuilder<WildcardPermission> {
private static ShiroPermissionBuilderImpl instance;
    
    public static ShiroPermissionBuilderImpl getInstance() {
        if (instance == null) {
            instance = new ShiroPermissionBuilderImpl();
        }
        return instance;
    }
    
    @Override
    public WildcardPermission getPermission(String objectType, Action action) {
        return new WildcardPermission(objectType + ":" + action.name(), true);
    }
    
    @Override
    public WildcardPermission getPermission(Class<?> objectType, Action action) {
        return getPermission(objectType.getName(), action);
    }
    
    @Override
    public WildcardPermission getPermission(String objectType, Action action, String objectId) {
        return new WildcardPermission(objectType + ":" + action.name() + ":" + objectId, true);
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
