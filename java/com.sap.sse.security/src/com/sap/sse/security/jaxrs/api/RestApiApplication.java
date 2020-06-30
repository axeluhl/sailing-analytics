package com.sap.sse.security.jaxrs.api;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.sap.sse.security.exceptions.handler.OwnershipExceptionHandler;
import com.sap.sse.security.jaxrs.ShiroAuthorizationExceptionTo401ResponseMapper;

public class RestApiApplication extends Application {
    public Set<Class<?>> getClasses() {
        HashSet<Class<?>> classes = new HashSet<>();
        classes.add(SecurityResource.class);
        classes.add(UserGroupResource.class);
        classes.add(RoleResource.class);
        classes.add(OwnershipResource.class);
        classes.add(PreferencesResource.class);
        // exception mapper
        classes.add(ShiroAuthorizationExceptionTo401ResponseMapper.class);
        classes.add(OwnershipExceptionHandler.class);
        return classes;
    }
}
