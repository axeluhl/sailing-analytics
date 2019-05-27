package com.sap.sse.security.jaxrs;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.shiro.authz.AuthorizationException;

@Provider
public class ShiroAuthorizationExceptionTo401ResponseMapper implements ExceptionMapper<AuthorizationException> {
    private static final Logger logger = Logger.getLogger(ShiroAuthorizationExceptionTo401ResponseMapper.class.getName());
    @Override
    public Response toResponse(AuthorizationException exception) {
        logger.log(Level.WARNING, "AuthorizationException occurred in REST call: " + exception.getMessage());
        return Response.status(Status.UNAUTHORIZED).entity(exception.getMessage()).build();
    }
}
