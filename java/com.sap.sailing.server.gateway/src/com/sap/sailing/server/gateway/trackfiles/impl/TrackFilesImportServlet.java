package com.sap.sailing.server.gateway.trackfiles.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.trackimport.GPSFixImporter;
import com.sap.sailing.server.gateway.impl.AbstractFileUploadServlet;
import com.sap.sse.common.Util.Pair;

/**
 * Servlet that processes uploaded track files by adding their fixes to the GPSFixStore. Returns a newline-separated
 * list of the device identifiers genearted by the import.
 * <p>
 * 
 * The available importers are tried one by one in the following order, until the first one is found that does not fail
 * with an {@link FormatNotSupportedException}:
 * <ul>
 * <li>If the type of a {@link #PREFERRED_IMPORTER preferred importer} is transmitted, this is the first that is used.
 * </li>
 * <li>Then the importers registered for a matching {@link GPSFixImporter#FILE_EXTENSION_PROPERTY file extension} are
 * used.</li>
 * <li>If all this fails, all other available importers are used.</li>
 * </ul>
 * 
 * @author Fredrik Teschke
 * 
 */
public class TrackFilesImportServlet extends AbstractFileUploadServlet {
    public static final String PREFERRED_IMPORTER = "preferredImporter";
    private static final long serialVersionUID = 1120226743039934620L;
    private static final Logger logger = Logger.getLogger(TrackFilesImportServlet.class.getName());

    @Override
    protected void process(List<FileItem> fileItems, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        ImportResult importResult = new ImportResult(logger);
        try {
            String prefImporterType = null;
            List<Pair<String, FileItem>> files = new ArrayList<>();
            for (FileItem item : fileItems) {
                if (!item.isFormField())
                    files.add(new Pair<String, FileItem>(item.getName(), item));
                else {
                    if (item.getFieldName() != null && item.getFieldName().equals(PREFERRED_IMPORTER)) {
                        prefImporterType = item.getString();
                    }
                }
            }
            new TrackFilesImporter(getService(), getServiceFinderFactory(), getContext()).importFixes(importResult,
                    prefImporterType, files);
            // setJsonResponseHeader(resp);
            // DO NOT set a JSON response header. This causes the browser to wrap the response in a
            // <pre> tag when uploading from GWT, as this is an AJAX-request inside an iFrame.
        } catch (Exception e) {
            importResult.add(e);
        } finally {
            resp.setContentType("text/html;charset=UTF-8");
            ImportResultSerializer.serializeImportResult(importResult).writeJSONString(resp.getWriter());
        }
    }
}
