package com.sap.sailing.server.gateway.windimport;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.server.gateway.SailingServerHttpServlet;
import com.sap.sailing.server.gateway.windimport.AbstractWindImporter.UploadRequest;
import com.sap.sailing.server.gateway.windimport.AbstractWindImporter.WindImportResult;

public abstract class AbstractWindImportServlet extends SailingServerHttpServlet {
    private static final Logger logger = Logger.getLogger(AbstractWindImportServlet.class.getName());
    private static final long serialVersionUID = 1L;
    
    private final AbstractWindImporter importer;
    
    public AbstractWindImportServlet(AbstractWindImporter importer) {
        this.importer = importer;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        if (!ServletFileUpload.isMultipartContent(request)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        WindImportResult windImportResult = new WindImportResult();
        try {
            UploadRequest uploadRequest = readRequest(request);
            importer.importWindForUploadRequest(getService(), windImportResult, uploadRequest);
            // Use text/html to prevent browsers from wrapping the response body,
            // see "Handling File Upload Responses in GWT" at http://www.artofsolving.com/node/50
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error ocurred trying to import wind fixes", e);
            windImportResult.error = e.toString();
        }
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().append(windImportResult.json().toJSONString());
    }

    private UploadRequest readRequest(HttpServletRequest req) throws FileUploadException, ParseException {
        UploadRequest result = new UploadRequest();
        // http://commons.apache.org/fileupload/using.html
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        @SuppressWarnings("unchecked")
        List<FileItem> items = upload.parseRequest(req);
        for (FileItem item : items) {
            if (item.isFormField() && (item.getString() != null) && (item.getString().trim().length() > 0)) {
                if ("boatId".equals(item.getFieldName())) {
                    result.boatId = item.getString().trim();
                } else if ("races".equals(item.getFieldName())) {
                    JSONArray races = (JSONArray) new JSONParser().parse(item.getString().trim());
                    for (Object raceEntry : races) {
                        result.races.add(new RegattaNameAndRaceName((String) ((JSONObject) raceEntry).get("regatta"),
                                (String) ((JSONObject) raceEntry).get("race")));
                    }
                }
            } else if (item.getSize() > 0) {
                result.files.add(item);
            }
        }
        return result;
    }
}
