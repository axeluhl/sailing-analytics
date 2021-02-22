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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sse.common.fileupload.FileUploadConstants;
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

    /**
     * The maximum size of an image uploaded by a user as a team image, in megabytes (1024*1024 bytes)
     */
    private static final long MAX_SIZE_IN_MB = 8192;

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
            } else if (fileType.startsWith("video/quicktime")) {
                // Quicktime/MOV is not supported in HTML5. 
                // Change file type to mp4 (which should work in most cases) is solving the problem.
                // Else it would be necessary to convert the file, which is not possible without an high amount
                // of effort.
                fileExtension = ".mp4";
            } else if (fileType.startsWith("video/")) {
                fileExtension = "."+fileType.substring(fileType.indexOf('/')+1);
            } else {
                int lastDot = fileName.lastIndexOf(".");
                if (lastDot > 0) {
                    fileExtension = fileName.substring(lastDot);
                } else {
                    fileExtension = "";
                }
            }
            try {
                if (fileItem.getSize() > 1024l * 1024l * MAX_SIZE_IN_MB) {
                    final String errorMessage = "Image is larger than " + MAX_SIZE_IN_MB + "MB";
                    logger.warning("Ignoring file storage request because file "+fileName+" is larger than "+MAX_SIZE_IN_MB+"MB");
                    result.put(FileUploadConstants.STATUS, Status.INTERNAL_SERVER_ERROR.name());
                    result.put(FileUploadConstants.MESSAGE, errorMessage);
                } else {
                    final URI fileUri = getService().getFileStorageManagementService().getActiveFileStorageService()
                            .storeFile(fileItem.getInputStream(), fileExtension, fileItem.getSize());
                    result.put(FileUploadConstants.FILE_NAME, fileName);
                    result.put(FileUploadConstants.FILE_URI, fileUri.toString());
                }
            } catch (IOException | OperationFailedException | RuntimeException | InvalidPropertiesException e) {
                final String errorMessage = "Could not store file"+ (e.getMessage()==null?"":(": " + e.getMessage()));
                logger.log(Level.WARNING, "Could not store file", e);
                result.put(FileUploadConstants.STATUS, Status.INTERNAL_SERVER_ERROR.name());
                result.put(FileUploadConstants.MESSAGE, errorMessage);
            }
            resultList.add(result);
        }
        // | surprise, surprise: see https://www.sencha.com/forum/showthread.php?132949-Fileupload-Invalid-JSON-string
        // | When sending a JSON response for a file upload, don't use application/json as the content type. It would lead
        // | to wrapping the content by a <pre> tag. Use text/html instead which should deliver the content to the app running
        // | in the browser unchanged.
        // This not true in detail. The response is pure json without <pre> tag before. But the form submit is interpreting the 
        // result and adding some HTML tags for displaying the content.
        // If text/html is returned, the browser tries to interpret the content, e.g. on mobile phone a large number in response 
        // will be interpreted as telephone number and is than wrapped into an <a> element. Therefore it it moved back to json. 
        // The logic reading the response already ignore the <pre> tag. So it should be no problem to use this with application/json.
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resultList.writeJSONString(resp.getWriter());
    }
}
