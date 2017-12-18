package com.sap.sailing.server.gateway.trackfiles.impl;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import com.sap.sailing.server.gateway.impl.AbstractFileUploadServlet;
import com.sap.sailing.server.gateway.trackfiles.impl.ExpeditionAllInOneImporter.ImporterResult;

/**
 * Import servlet for sensor data files. Importers are located through the OSGi service registry and matched against the
 * name provided by the upload form.
 */
public class ExpeditionAllInOneImportServlet extends AbstractFileUploadServlet {
    private static final long serialVersionUID = 1120226743039934620L;
    private static final Logger logger = Logger.getLogger(ExpeditionAllInOneImportServlet.class.getName());

    /**
     * Process the uploaded file items.
     */
    @Override
    protected void process(List<FileItem> fileItems, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        JsonHolder jsonResult = new JsonHolder(logger);
        try {
            String fileName = null;
            FileItem fileItem = null;
            for (FileItem fi : fileItems) {
                if (!fi.isFormField()) {
                    fileName = fi.getName();
                    fileItem = fi;
                }
            }
            if (fileItem == null) {
                throw new RuntimeException("No file to import");
            }
            ImporterResult importerResult = new ExpeditionAllInOneImporter(getService(), /* TODO */ null, getServiceFinderFactory(), getContext()).importFiles(fileName, fileItem);
        } catch (Exception e) {
            jsonResult.add(e);
        } finally {
            jsonResult.writeJSONString(resp);
        }
    }
}
