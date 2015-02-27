package com.sap.sailing.server.gateway.jaxrs.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.OperationFailedException;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

@Path("/v1/file")
public class FileStorageResource extends AbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(FileStorageResource.class.getName());
    
    public static final String JSON_FILE_URI = "file_uri";
    
    /**
     * The maximum size of an image uploaded by a user as a team image, in megabytes (1024*1024 bytes)
     */
    private static final int MAX_SIZE_IN_MB = 5;

    /**
     * Expects the HTTP header {@code Content-Length} to be set.
     */
    //
    // example for testing upload:
    // $ curl -v -H "Content-Type:image/jpeg" \
    //     --data-binary @<path-to-local-jpg> \
    //     http://127.0.0.1:8888/sailingserver/api/v1/file
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("text/html;charset=UTF-8")
    public String uploadFile(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataBodyPart filePart) {
        final JSONObject result = new JSONObject();
        String fileExtension = "";
        MediaType fileType = filePart.getMediaType();
        if (fileType.getType().equals("image")) {
            if (fileType.getSubtype().equals("jpeg")) {
                fileExtension = ".jpg";
            } else if (fileType.getSubtype().equals("png")) {
                fileExtension = ".png";
            }
        }

        URI fileUri;
        try {
            if (filePart.getContentDisposition().getSize() > 1024 * 1024 * MAX_SIZE_IN_MB) {
                throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
                        .entity("Image is larger than " + MAX_SIZE_IN_MB + "MB").build());
            }
            fileUri = getService().getFileStorageManagementService().getActiveFileStorageService()
                    .storeFile(uploadedInputStream, fileExtension, filePart.getContentDisposition().getSize());
            result.put(JSON_FILE_URI, fileUri.toString());
        } catch (IOException | OperationFailedException | InvalidPropertiesException e) {
            final String errorMessage = "Could not store file: "+e.getMessage();
            logger.log(Level.WARNING, "Could not store file", e);
            result.put("status", Status.INTERNAL_SERVER_ERROR.name());
            result.put("message", errorMessage);
        }
        return result.toJSONString();
    }
    
    // Example test use:
    //     curl -X DELETE http://127.0.0.1:8888/sailingserver/api/v1/file?uri=file:///c:/tmp/c7b821e1-ebab-4a96-a71d-28ac192b3e69.jpg
    @DELETE
    @Produces("application/json;charset=UTF-8")
    public Response deleteImage(@QueryParam("uri") String uri) {
        final JSONObject result = new JSONObject();
        Response response;
        try {
            getService().getFileStorageManagementService().getActiveFileStorageService()
                    .removeFile(new URI(uri));
            result.put("status", Status.OK.name());
            response = Response.ok().entity(result.toJSONString()).build();
        } catch (NoCorrespondingServiceRegisteredException | OperationFailedException | InvalidPropertiesException
                | URISyntaxException | IOException e) {
            final String errorMessage = "Could not delete file "+uri+": "+e.getMessage();
            logger.log(Level.WARNING, "Could not delete file "+uri, e);
            result.put("status", Status.BAD_REQUEST.name());
            result.put("message", errorMessage);
            response = Response.status(Status.BAD_REQUEST).entity(result.toJSONString()).build();
        }
        return response;
    }
}
