package com.sap.sailing.server.gateway.trackfiles.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;

/**
 * Convenience class that wraps the json objects used to render json result objects
 */
public class JsonHolder {
    private final Set<UUID> knownMapings = new HashSet<>();
    private final JSONObject jsonResponseObj = new JSONObject();
    private final JSONArray jsonErrorObj = new JSONArray();
    private final JSONArray jsonUuidObj = new JSONArray();
    private final Logger logger;

    public JsonHolder(Logger logger) {
        this.logger = logger;
        jsonResponseObj.put("errors", jsonErrorObj);
        jsonResponseObj.put("uploads", jsonUuidObj);
    }

    public void add(String requestedImporterName, String filename, Exception exception) {
        logger.log(Level.SEVERE, "Sensordata import importer: " + requestedImporterName);
        logger.log(Level.SEVERE, "Sensordata import filename: " + filename);
        JSONObject jsonExceptionObj = logException(exception);
        jsonExceptionObj.put("filename", filename);
        jsonExceptionObj.put("requestedImporter", requestedImporterName);
    }

    public void add(Exception exception) {
        logException(exception);
    }

    private JSONObject logException(Exception e) {
        final String exUUID = UUID.randomUUID().toString();
        logger.log(Level.SEVERE, "Sensordata import ExUUID: " + exUUID, e);
        JSONObject jsonExceptionObj = new JSONObject();
        jsonErrorObj.add(jsonExceptionObj);
        jsonExceptionObj.put("exUUID", exUUID);
        jsonExceptionObj.put("className", e.getClass().getName());
        jsonExceptionObj.put("message", e.getMessage());
        return jsonExceptionObj;
    }

    /**
     * 
     * @param deviceIdentifier
     */
    public void addDeviceIndentifier(TrackFileImportDeviceIdentifier deviceIdentifier) {
        if (!knownMapings.contains(deviceIdentifier.getId())) {
            knownMapings.add(deviceIdentifier.getId());
            String stringRep = deviceIdentifier.getId().toString();
            jsonUuidObj.add(stringRep);
        }
    }

    public void writeJSONString(HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        jsonResponseObj.writeJSONString(resp.getWriter());
    }
}