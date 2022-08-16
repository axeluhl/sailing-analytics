package com.sap.sse.security.jaxrs;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;

@Provider
public class ShiroAuthorizationExceptionTo401ResponseMapper implements ExceptionMapper<AuthorizationException> {
    private static final Logger logger = Logger.getLogger(ShiroAuthorizationExceptionTo401ResponseMapper.class.getName());
    @Override
    public Response toResponse(AuthorizationException exception) {
        final Subject subject = SecurityUtils.getSubject();
        final String principalAsString;
        if (subject != null && subject.getPrincipal() != null) {
            principalAsString = subject.getPrincipal().toString();
        } else {
            principalAsString = null;
        }
        logger.log(Level.WARNING, "AuthorizationException occurred in REST call"+
                (principalAsString==null?"":(" for user "+principalAsString)+
                ": " + exception.getMessage()));
        return Response.status(Status.UNAUTHORIZED).entity(exception.getMessage()).build();
    }
}
