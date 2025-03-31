package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sse.common.fileupload.FileUploadUtil;

/**
 * Abstract servlet handling multipart-mime file upload coming from an upload form. The response content type is set to
 * "text/html" by default (see {@link #setJsonEncodedInHtmlResponseHeader(HttpServletResponse)}). If your response is a
 * JSON document, encode it as an HTML document {@code body} using
 * {@link FileUploadUtil#getHtmlWithEmbeddedJsonContent(String)}. A client can then use the submit complete event
 * response and decode it using {@link FileUploadUtil#getApplicationJsonContentFromHtml(String)}.
 * 
 * @author Fredrik Teschke
 * @author Axel Uhl
 * 
 */
public abstract class AbstractFileUploadServlet extends AbstractJsonHttpServlet {
    public static final String PREFERRED_IMPORTER = "preferredImporter";
    public static final String PROGRESS_LISTENER_SESSION_ATTRIBUTE_NAME = "progressListener";
    private static final long serialVersionUID = 1120226743039934620L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!ServletFileUpload.isMultipartContent(req)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        HttpSession session = req.getSession(true);
        if (session != null) {
            ProgressListener progressListener = new ProgressListener();
            upload.setProgressListener(progressListener);
            session.setAttribute(PROGRESS_LISTENER_SESSION_ATTRIBUTE_NAME, progressListener);
        }
        try {
            @SuppressWarnings("unchecked")
            List<FileItem> items = (List<FileItem>) upload.parseRequest(req);
            // The content type and character encoding are expected to be set by AbstractFileUploadServlet.doPost already
            // before invoking this method. The hairy part about all this is that we trigger file uploads from <form>
            // elements in HTML pages, and by default these expect to receive content of type text/html, or at best
            // application/xml, but application/json does not belong to the expected content types of an HTML form.
            // This leads to the strange effect that browser which expect to need to display the response to a user
            // will wrap the application/json content in a <pre> tag even before delivering it to the onSubmitComplete handler.
            // Therefore, all such handlers must ensure they check for a <pre>...</pre> enclosing and remove it before
            // handling the content.
            // Conversely, trying to use text/html as content encoding leads some browsers---especially on mobile devices---
            // to do ugly things to the content returned, such as replacing digit sequences by a corresponding <a> element
            // that allows the user to dial that number with the phone app...
            setJsonEncodedInHtmlResponseHeader(resp);
            process(items, req, resp);
        } catch (FileUploadException e) {
            throw new IOException("Could not parse request");
        }
    }

    abstract protected void process(List<FileItem> fileItems, HttpServletRequest req, HttpServletResponse resp) throws IOException;
    
}
