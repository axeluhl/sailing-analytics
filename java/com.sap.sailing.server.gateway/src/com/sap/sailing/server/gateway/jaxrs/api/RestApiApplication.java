package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.sap.sse.security.jaxrs.ShiroAuthorizationExceptionTo401ResponseMapper;


public class RestApiApplication extends Application {
    public Set<Class<?>> getClasses() {
        HashSet<Class<?>> classes = new HashSet<>();
        // RESTlets
        classes.add(LeaderboardGroupsResource.class);
        classes.add(EventsResource.class);
        classes.add(RegattasResource.class);
        classes.add(LeaderboardsResource.class);
        classes.add(PolarResource.class);
        classes.add(SearchResource.class);
        classes.add(GPSFixesResource.class);
        classes.add(CompetitorsResource.class);
        
        // Exception Mapper
        classes.add(ShiroAuthorizationExceptionTo401ResponseMapper.class);
        return classes;
    }
}
