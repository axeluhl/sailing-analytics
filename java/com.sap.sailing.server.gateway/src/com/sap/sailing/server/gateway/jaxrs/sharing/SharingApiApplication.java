package com.sap.sailing.server.gateway.jaxrs.sharing;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.sap.sailing.server.gateway.jaxrs.exceptions.ExceptionManager;
import com.sap.sse.security.jaxrs.ShiroAuthorizationExceptionTo401ResponseMapper;

public class SharingApiApplication extends Application {
    public Set<Class<?>> getClasses() {
        HashSet<Class<?>> classes = new HashSet<>();
        // RESTlets
        classes.add(HomeSharingResource.class);
        // Exception Mappers
        classes.add(ShiroAuthorizationExceptionTo401ResponseMapper.class);
        classes.add(ExceptionManager.class);
        return classes;
    }
}
