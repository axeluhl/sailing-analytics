package com.sap.sailing.domain.igtimiadapter.gateway.oauth;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.sap.sse.security.jaxrs.ShiroAuthorizationExceptionTo401ResponseMapper;

public class RestApiApplication extends Application {
    public RestApiApplication() {
    }

    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(new Class<?>[] { AuthorizationCallback.class, ShiroAuthorizationExceptionTo401ResponseMapper.class }));
    }
}
