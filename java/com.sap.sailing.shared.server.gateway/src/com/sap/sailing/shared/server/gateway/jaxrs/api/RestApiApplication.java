package com.sap.sailing.shared.server.gateway.jaxrs.api;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.sap.sailing.shared.server.gateway.jaxrs.exceptions.ExceptionManager;
import com.sap.sse.security.jaxrs.ShiroAuthorizationExceptionTo401ResponseMapper;

public class RestApiApplication extends Application {
    public Set<Class<?>> getClasses() {
        HashSet<Class<?>> classes = new HashSet<>();
        // RESTlets
        classes.add(MarkPropertiesResource.class);
        classes.add(MarkTemplateResource.class);
        classes.add(CourseTemplateResource.class);
        classes.add(MarkRoleResource.class);
        
        // Exception Mappers
        classes.add(ShiroAuthorizationExceptionTo401ResponseMapper.class);
        classes.add(ExceptionManager.class);
        return classes;
    }
}
