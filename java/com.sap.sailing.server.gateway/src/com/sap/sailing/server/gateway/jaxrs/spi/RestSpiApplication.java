package com.sap.sailing.server.gateway.jaxrs.spi;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;


public class RestSpiApplication extends Application {
    public Set<Class<?>> getClasses() {
        HashSet<Class<?>> classes = new HashSet<>();
        classes.add(MasterDataResource.class);
        return classes;
    }
}
