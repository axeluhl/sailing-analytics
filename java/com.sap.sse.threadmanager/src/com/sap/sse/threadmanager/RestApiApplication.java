package com.sap.sse.threadmanager;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class RestApiApplication extends Application {
    public Set<Class<?>> getClasses() {
        HashSet<Class<?>> classes = new HashSet<>();
        classes.add(ThreadManager.class);
        return classes;
    }
}
