package com.sap.sailing.windestimation.jaxrs.api;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.sap.sse.security.jaxrs.ShiroAuthorizationExceptionTo401ResponseMapper;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class RestApiApplication extends Application {
        
    public Set<Class<?>> getClasses() {
        HashSet<Class<?>> classes = new HashSet<>();
        // RESTlets
        classes.add(WindEstimationDataResource.class);
        // Exception Mapper
        classes.add(ShiroAuthorizationExceptionTo401ResponseMapper.class);
        return classes;
    }
}
