package com.sap.sailing.server.gateway.trackfiles.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.json.simple.JSONObject;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapterFactory;
import com.sap.sailing.server.gateway.impl.AbstractFileUploadServlet;
import com.sap.sailing.server.gateway.trackfiles.impl.ExpeditionAllInOneImporter.ImporterResult;
import com.sap.sse.util.ServiceTrackerFactory;

/**
 * Import servlet for sensor data files. Importers are located through the OSGi service registry and matched against the
 * name provided by the upload form.
 */
public class ExpeditionAllInOneImportServlet extends AbstractFileUploadServlet {
    private static final long serialVersionUID = 1120226743039934620L;
    // private static final Logger logger = Logger.getLogger(ExpeditionAllInOneImportServlet.class.getName());
    private ServiceTracker<RaceLogTrackingAdapterFactory, RaceLogTrackingAdapterFactory> raceLogTrackingAdapterTracker;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        raceLogTrackingAdapterTracker = ServiceTrackerFactory.createAndOpen(getContext(),
                RaceLogTrackingAdapterFactory.class);
    }

    /**
     * Process the uploaded file items.
     */
    @Override
    protected void process(List<FileItem> fileItems, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        ImporterResult importerResult = null;
        try {
            String fileName = null;
            FileItem fileItem = null;
            String boatClassName = null;
            for (FileItem fi : fileItems) {
                if (!fi.isFormField()) {
                    fileName = fi.getName();
                    fileItem = fi;
                } else if (fi.getFieldName() != null) {
                    if ("boatClass".equals(fi.getFieldName())) {
                        boatClassName = fi.getString();
                    }
                }
            }
            if (fileItem == null) {
                throw new RuntimeException("No file to import");
            }
            importerResult = new ExpeditionAllInOneImporter(getService(),
                    raceLogTrackingAdapterTracker.getService().getAdapter(getService().getBaseDomainFactory()),
                    getServiceFinderFactory(), getContext()).importFiles(fileName, fileItem, boatClassName);
        } catch (AllinOneImportException e) {
            importerResult = new ImporterResult(e, e.additionalErrors);
        } catch (Throwable t) {
            importerResult = new ImporterResult(t, Collections.emptyList());
        } finally {
            this.toJSON(importerResult).writeJSONString(resp.getWriter());
        }
    }

    private JSONObject toJSON(ImporterResult importerResult) {
        final JSONObject json = new JSONObject();
        json.put("eventId", importerResult.eventId.toString());
        json.put("leaderboardName", importerResult.leaderboardName);
        json.put("leaderboardGroupName", importerResult.leaderboardGroupName);
        json.put("regattaName", importerResult.regattaName);
        json.put("raceName", importerResult.raceName);
        json.put("raceColumnName", importerResult.raceColumnName);
        json.put("fleetName", importerResult.fleetName);
        json.put("gpsDeviceIds", ImportResultSerializer.serializeTrackList(importerResult.importGpsFixData));
        json.put("sensorDeviceIds", ImportResultSerializer.serializeTrackList(importerResult.importSensorFixData));
        json.put("sensorFixImporterType", importerResult.sensorFixImporterType);
        return json;
    }
}
