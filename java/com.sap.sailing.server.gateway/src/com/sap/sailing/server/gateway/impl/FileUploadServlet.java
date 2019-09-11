package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.fileupload.FileItem;
import org.apache.shiro.authz.UnauthorizedException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.OperationFailedException;

/**
 * Accepts a multi-part MIME encoded set of files. Returns an array of JSONObjects that each contain
 * a "file_uri" value, in the same order in which the parts were sent.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class FileUploadServlet extends AbstractFileUploadServlet {
    private static final long serialVersionUID = -9002541098579359029L;

    private static final Logger logger = Logger.getLogger(FileUploadServlet.class.getName());

    public static final String JSON_FILE_NAME = "file_name";
    
    public static final String JSON_FILE_URI = "file_uri";

    /**
     * The maximum size of an image uploaded by a user as a team image, in megabytes (1024*1024 bytes)
     */
    private static final int MAX_SIZE_IN_MB = 500;

    @Override
    protected void process(List<FileItem> fileItems, HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException, IOException {
        /**
         * Expects the HTTP header {@code Content-Length} to be set.
         */
        final JSONArray resultList = new JSONArray();
        for (FileItem fileItem : fileItems) {
            final JSONObject result = new JSONObject();
            final String fileExtension;
            final String fileName = Paths.get(fileItem.getName()).getFileName().toString();
            final String fileType = fileItem.getContentType();
            if (fileType.equals("image/jpeg")) {
                fileExtension = ".jpg";
            } else if (fileType.equals("image/png")) {
                fileExtension = ".png";
            } else if (fileType.equals("image/gif")) {
                fileExtension = ".gif";
            } else if (fileType.startsWith("video/")) {
                fileExtension = fileType.substring(fileType.indexOf('/')+1);
            } else {
                int lastDot = fileName.lastIndexOf(".");
                if (lastDot > 0) {
                    fileExtension = fileName.substring(lastDot);
                } else {
                    fileExtension = "";
                }
            }
            try {
                if (fileItem.getSize() > 1024 * 1024 * MAX_SIZE_IN_MB) {
                    final String errorMessage = "Image is larger than " + MAX_SIZE_IN_MB + "MB";
                    logger.warning("Ignoring file storage request because file "+fileName+" is larger than "+MAX_SIZE_IN_MB+"MB");
                    result.put("status", Status.INTERNAL_SERVER_ERROR.name());
                    result.put("message", errorMessage);
                } else {
                    final URI fileUri = getService().getFileStorageManagementService().getActiveFileStorageService()
                            .storeFile(fileItem.getInputStream(), fileExtension, fileItem.getSize());
                    result.put(JSON_FILE_NAME, fileName);
                    result.put(JSON_FILE_URI, fileUri.toString());
                }
            } catch (IOException | OperationFailedException | InvalidPropertiesException
                    | NoCorrespondingServiceRegisteredException | UnauthorizedException e) {
                final String errorMessage = "Could not store file"+ (e.getMessage()==null?"":(": " + e.getMessage()));
                logger.log(Level.WARNING, "Could not store file", e);
                result.put("status", Status.INTERNAL_SERVER_ERROR.name());
                result.put("message", errorMessage);
            }
            resultList.add(result);
        }
        // surprise, surprise: see https://www.sencha.com/forum/showthread.php?132949-Fileupload-Invalid-JSON-string
        // When sending a JSON response for a file upload, don't use application/json as the content type. It would lead
        // to wrapping the content by a <pre> tag. Use text/html instead which should deliver the content to the app running
        // in the browser unchanged.
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().append(resultList.toJSONString());
    }
}
