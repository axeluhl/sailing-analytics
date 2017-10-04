package com.sap.sse.security.shared;

import com.sap.sse.common.WithID;

public interface PermissionBuilder<T> {
    public static interface Action {
        String name();
    }
    
    public enum DefaultActions implements Action {
        CREATE, VIEW, EDIT, REMOVE;
    }
    
    public T getPermission(String objectType, Action action);
    
    public T getPermission(Class<?> objectType, Action action);
    
    public T getPermission(String objectType, Action action, String objectId);
    
    public T getPermission(Class<?> objectType, Action action, String objectId);
    
    public T getPermission(Class<?> objectType, Action action, WithID object);
    
    public T getPermission(WithID object, Action action);
}
