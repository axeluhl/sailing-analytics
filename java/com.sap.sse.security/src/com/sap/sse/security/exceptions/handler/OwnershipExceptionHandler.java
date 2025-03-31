package com.sap.sse.security.exceptions.handler;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.sap.sse.security.exceptions.OwnershipException;
import com.sap.sse.security.jaxrs.ShiroAuthorizationExceptionTo401ResponseMapper;
import com.sap.sse.security.model.GeneralResponse;


/***
 * This class is used to handle {@link OwnershipException} thrown from any API of project.
 * @author Usman Ali
 *
 */
@Provider
public class OwnershipExceptionHandler implements ExceptionMapper<OwnershipException> {
    private static final Logger logger = Logger.getLogger(ShiroAuthorizationExceptionTo401ResponseMapper.class.getName());
    @Override
    public Response toResponse(OwnershipException exception) {
        logger.log(Level.WARNING, "OwnershipException occurred in REST call: " + exception.getMessage());
        return Response.status(exception.getStatus()).entity(new GeneralResponse(false, exception.getMessage()).toString()).build();
    }
}
