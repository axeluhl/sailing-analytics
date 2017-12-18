package com.sap.sailing.server.gateway.trackfiles.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.trackfiles.impl.ImportResultDTO.ErrorImportDTO;
import com.sap.sailing.server.gateway.trackfiles.impl.ImportResultDTO.TrackImportDTO;

/**
 * Convenience class that wraps the json objects used to render json result objects
 */
public class ImportResultSerializer {
    private final JSONObject jsonResponseObj = new JSONObject();
    private final JSONArray jsonErrorObj = new JSONArray();
    private final JSONArray jsonUuidObj = new JSONArray();

    public ImportResultSerializer(ImportResultDTO data) {
        jsonResponseObj.put("errors", jsonErrorObj);
        jsonResponseObj.put("uploads", jsonUuidObj);
        for(ErrorImportDTO error:data.getErrorList()){
            JSONObject jsonExceptionObj = new JSONObject();
            jsonExceptionObj.put("filename", error.getFilename());
            jsonExceptionObj.put("requestedImporter", error.getRequestedImporter());
            jsonExceptionObj.put("exUUID", error.getExUUID());
            jsonExceptionObj.put("className", error.getName());
            jsonExceptionObj.put("message", error.getMessage());
            jsonErrorObj.add(jsonExceptionObj);
        }
        for(TrackImportDTO result:data.getImportResult()){
            String stringRep = result.getDevice().toString();
            jsonUuidObj.add(stringRep);
        }
    }

    public void writeJSONString(HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        jsonResponseObj.writeJSONString(resp.getWriter());
    }
}