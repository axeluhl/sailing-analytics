package com.sap.sailing.server.gateway.trackfiles.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.json.simple.JSONObject;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.common.dto.ExpeditionAllInOneConstants;
import com.sap.sailing.domain.common.dto.ExpeditionAllInOneConstants.ImportMode;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapterFactory;
import com.sap.sailing.server.gateway.impl.AbstractFileUploadServlet;
import com.sap.sailing.server.gateway.trackfiles.impl.ExpeditionAllInOneImporter.ImporterResult;
import com.sap.sse.i18n.ResourceBundleStringMessages;
import com.sap.sse.i18n.impl.ResourceBundleStringMessagesImpl;
import com.sap.sse.util.ServiceTrackerFactory;

/**
 * Import servlet for sensor data files. Importers are located through the OSGi service registry and matched against the
 * name provided by the upload form.
 * 
 * @see ExpeditionAllInOneImporter
 */
public class ExpeditionAllInOneImportServlet extends AbstractFileUploadServlet {
    private static final long serialVersionUID = 1120226743039934620L;
    private static final Logger logger = Logger.getLogger(ExpeditionAllInOneImportServlet.class.getName());

    private static final String STRING_MESSAGES_BASE_NAME = "stringmessages/StringMessages";

    private ServiceTracker<RaceLogTrackingAdapterFactory, RaceLogTrackingAdapterFactory> raceLogTrackingAdapterTracker;
    private ResourceBundleStringMessages serverStringMessages;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        raceLogTrackingAdapterTracker = ServiceTrackerFactory.createAndOpen(getContext(),
                RaceLogTrackingAdapterFactory.class);
        serverStringMessages = new ResourceBundleStringMessagesImpl(STRING_MESSAGES_BASE_NAME,
                this.getClass().getClassLoader(), StandardCharsets.UTF_8.name());
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
            String regattaName = null;
            String importModeName = null;
            String localeName = null;
            for (FileItem fi : fileItems) {
                if (!fi.isFormField()) {
                    fileName = fi.getName();
                    fileItem = fi;
                } else if (fi.getFieldName() != null) {
                    if (ExpeditionAllInOneConstants.REQUEST_PARAMETER_BOAT_CLASS.equals(fi.getFieldName())) {
                        boatClassName = fi.getString();
                    }
                    if (ExpeditionAllInOneConstants.REQUEST_PARAMETER_REGATTA_NAME.equals(fi.getFieldName())) {
                        regattaName = fi.getString();
                    }
                    if (ExpeditionAllInOneConstants.REQUEST_PARAMETER_IMPORT_MODE.equals(fi.getFieldName())) {
                        importModeName = fi.getString();
                    }
                    if (ExpeditionAllInOneConstants.REQUEST_PARAMETER_LOCALE.equals(fi.getFieldName())) {
                        localeName = fi.getString();
                    }
                }
            }
            Locale uiLocale;
            if (localeName == null) {
                uiLocale = Locale.ENGLISH;
            } else {
                try {
                    uiLocale = Locale.forLanguageTag(localeName);
                } catch (Exception e) {
                    uiLocale = Locale.ENGLISH;
                }
            }
            if (fileItem == null) {
                throw new AllinOneImportException(serverStringMessages.get(uiLocale, "allInOneErrorImportFileMissing"));
            }
            final ImportMode importMode;
            if (importModeName == null) {
                importMode = ImportMode.NEW_EVENT;
            } else {
                try {
                    importMode = ImportMode.valueOf(importModeName);
                } catch (Exception e) {
                    throw new AllinOneImportException(serverStringMessages.get(uiLocale, "allInOneErrorUnknownImportMode"));
                }
            }
            if (importMode == ImportMode.NEW_EVENT) {
                if (boatClassName == null || boatClassName.isEmpty()) {
                    throw new AllinOneImportException(serverStringMessages.get(uiLocale, "allInOneErrorMissingBoatClass"));
                }
            } else {
                if (regattaName == null || regattaName.isEmpty()) {
                    throw new AllinOneImportException(serverStringMessages.get(uiLocale, "allInOneErrorMissingRegattaClass"));
                }
            }
            importerResult = new ExpeditionAllInOneImporter(serverStringMessages, uiLocale, getService(),
                    raceLogTrackingAdapterTracker.getService().getAdapter(getService().getBaseDomainFactory()),
                    getServiceFinderFactory(), getContext()).importFiles(fileName, fileItem, boatClassName, importMode, regattaName);
        } catch (AllinOneImportException e) {
            importerResult = new ImporterResult(e, e.additionalErrors);
            logger.log(Level.SEVERE, e.getMessage());
        } catch (Throwable t) {
            importerResult = new ImporterResult(t, Collections.emptyList());
            logger.log(Level.SEVERE, t.getMessage());
        } finally {
            this.toJSON(importerResult).writeJSONString(resp.getWriter());
        }
    }

    private JSONObject toJSON(ImporterResult importerResult) {
        final JSONObject json = new JSONObject();
        json.put("eventId", importerResult.eventId == null ? null : importerResult.eventId.toString());
        json.put("leaderboardName", importerResult.leaderboardName);
        json.put("leaderboardGroupName", importerResult.leaderboardGroupName);
        json.put("regattaName", importerResult.regattaName);
        json.put("raceName", importerResult.raceName);
        json.put("raceColumnName", importerResult.raceColumnName);
        json.put("fleetName", importerResult.fleetName);
        json.put("errors", ImportResultSerializer.serializeErrorList(importerResult.errorList));
        json.put("gpsDeviceIds", ImportResultSerializer.serializeTrackList(importerResult.importGpsFixData));
        json.put("sensorDeviceIds", ImportResultSerializer.serializeTrackList(importerResult.importSensorFixData));
        json.put("sensorFixImporterType", importerResult.sensorFixImporterType);
        return json;
    }
}
