package com.sap.sailing.server.gateway.jaxrs.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.authz.UnauthorizedException;
import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.impl.FileUploadServlet;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.OperationFailedException;

/**
 * File upload is handled by {@link FileUploadServlet} because Jersey has issues with multipart MIME file posting. This
 * resource only handles the DELETE method.
 * 
 * @author Axel Uhl (d043530)
 *
 */
@Path("/v1/file")
public class FileStorageResource extends AbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(FileStorageResource.class.getName());
    
    @GET
    public Response getFile(@QueryParam("uri") String uri) {
        Response response;
        try {

            getService().getFileStorageManagementService().getActiveFileStorageService()
                    .doPermissionCheckForGetFile(new URI(uri));

            InputStream inputStream = new URL(uri).openStream();
            ResponseBuilder responseBuilder = Response.ok().entity(inputStream);
            if (uri.toLowerCase().endsWith(".jpg")) {
                responseBuilder.header("Content-Type", "image/jpeg");
            } else if (uri.toLowerCase().endsWith(".png")) {
                responseBuilder.header("Content-Type", "image/png");
            }
            response = responseBuilder.build();
        } catch (IOException | URISyntaxException ioe) {
            response = Response.status(Status.BAD_REQUEST).entity(ioe.getMessage()).build();
        } catch (UnauthorizedException e) {
            response = Response.status(Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
        return response;
    }
    
    // Example test use:
    //     curl -d "uri=file:///c:/tmp/c7b821e1-ebab-4a96-a71d-28ac192b3e69.jpg" http://127.0.0.1:8888/sailingserver/api/v1/file
    @DELETE
    @Produces("application/json;charset=UTF-8")
    public Response deleteFile(@QueryParam("uri") String uri) {
        final JSONObject result = new JSONObject();
        Response response;
        try {
            getService().getFileStorageManagementService().getActiveFileStorageService()
                    .removeFile(new URI(uri));
            result.put("status", Status.OK.name());
            response = Response.ok().entity(result.toJSONString()).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
        } catch (NoCorrespondingServiceRegisteredException | OperationFailedException | InvalidPropertiesException
                | URISyntaxException | IOException e) {
            final String errorMessage = "Could not delete file with URI "+uri+": "+e.getMessage();
            logger.log(Level.WARNING, "Could not delete file with URI "+uri, e);
            result.put("status", Status.BAD_REQUEST.name());
            result.put("message", errorMessage);
            response = Response.status(Status.BAD_REQUEST).entity(result.toJSONString()).build();
        }
        catch (UnauthorizedException e) {
            response = Response.status(Status.UNAUTHORIZED).entity(e.getMessage()).build();            
        }
        return response;
    }

    @POST
    @Produces("application/json;charset=UTF-8")
    public Response postDeleteFile(@QueryParam("uri") String uri) {
        return deleteFile(uri);
    }
}
