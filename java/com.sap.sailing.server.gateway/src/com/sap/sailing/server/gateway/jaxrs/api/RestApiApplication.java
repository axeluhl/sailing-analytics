package com.sap.sailing.server.gateway.impl.rs;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class RestApplication extends Application {
    public Set<Class<?>> getClasses() {
        HashSet<Class<?>> classes = new HashSet<>();
        classes.add(LeaderboardGroupsResource.class);
        classes.add(EventsResource.class);
        classes.add(RegattasResource.class);
        
        return classes;
    }
}
