package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;

/**
 * Abstract servlet handling multipart-mime file upload coming from an upload form
 * 
 * @author Fredrik Teschke
 * @author Axel Uhl
 * 
 */
public abstract class AbstractFileUploadServlet extends AbstractJsonHttpServlet {
    public static final String PREFERRED_IMPORTER = "preferredImporter";
    private static final long serialVersionUID = 1120226743039934620L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!ServletFileUpload.isMultipartContent(req)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        try {
            @SuppressWarnings("unchecked")
            List<FileItem> items = (List<FileItem>) upload.parseRequest(req);
            process(items, req, resp);
        } catch (FileUploadException e) {
            throw new IOException("Could not parse request");
        }
    }

    abstract protected void process(List<FileItem> fileItems, HttpServletRequest req, HttpServletResponse resp) throws IOException;
}
