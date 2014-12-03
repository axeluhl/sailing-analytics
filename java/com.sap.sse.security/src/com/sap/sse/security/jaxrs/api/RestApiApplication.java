package com.sap.sse.security.jaxrs.api;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class RestApiApplication extends Application {
    public Set<Class<?>> getClasses() {
        HashSet<Class<?>> classes = new HashSet<>();
        classes.add(SecurityResource.class);
        return classes;
    }
}
