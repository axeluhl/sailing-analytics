package com.sap.sailing.server.gateway.trackfiles.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import com.sap.sailing.server.gateway.impl.AbstractFileUploadServlet;
import com.sap.sse.common.Util.Pair;

/**
 * Import servlet for sensor data files. Importers are located through the OSGi service registry and matched against the
 * name provided by the upload form.
 */
public class SensorDataImportServlet extends AbstractFileUploadServlet {
    private static final long serialVersionUID = 1120226743039934620L;
    private static final Logger logger = Logger.getLogger(SensorDataImportServlet.class.getName());

    /**
     * Process the uploaded file items.
     */
    @Override
    protected void process(List<FileItem> fileItems, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        ImportResult importResult = new ImportResult(logger);
        boolean enableDownsampler = false;
        try {
            String importerName = null;
            for (FileItem fi : fileItems) {
                if ("preferredImporter".equalsIgnoreCase(fi.getFieldName())) {
                    importerName = fi.getString();
                } else if ("downsample".equalsIgnoreCase(fi.getFieldName())) {
                    enableDownsampler = "on".equalsIgnoreCase(fi.getString());
                }
            }
            if (importerName == null) {
                throw new RuntimeException("Missing preferred importer");
            }
            List<Pair<String, FileItem>> filesAndImporterNames = new ArrayList<>();
            for (FileItem fi : fileItems) {
                if ("file".equalsIgnoreCase(fi.getFieldName())) {
                    filesAndImporterNames.add(new Pair<>(importerName, fi));
                }
            }
            new SensorDataImporter(getService(), getContext()).importFiles(enableDownsampler, importResult, filesAndImporterNames);
        } catch (Exception e) {
            importResult.add(e);
        } finally {
            resp.setContentType("text/html;charset=UTF-8");
            ImportResultSerializer.serializeImportResult(importResult).writeJSONString(resp.getWriter());
        }
    }
}
