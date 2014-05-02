package com.sap.sailing.server.gateway.trackfiles.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONArray;

import com.sap.sailing.domain.common.racelog.tracking.TypeBasedServiceFinder;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.DeviceIdentifierJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;
import com.sap.sailing.server.gateway.trackfiles.TrackFileImportDeviceIdentifierImpl;
import com.sap.sailing.server.trackfiles.Import;
import com.sap.sailing.server.trackfiles.Import.FixCallback;

/**
 * Servlet that processes uploaded track files by adding their fixes to the GPSFixStore.
 * Returns a JSON array of generated device identifiers.
 * @author Fredrik Teschke
 *
 */
public class TrackFilesImportServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = 1120226743039934620L;
    
    private void storeFix(GPSFix fix, DeviceIdentifier deviceIdentifier) {
        getService().getGPSFixStore().storeFix(deviceIdentifier, fix);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!ServletFileUpload.isMultipartContent(req)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Set<FileItem> files = new HashSet<>();
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items;
        try {
            items = (List<FileItem>) upload.parseRequest(req);
        } catch (FileUploadException e) {
            throw new IOException("Could not parse request");
        }
        for (FileItem item : items) {
            if (!item.isFormField())
                files.add(item);
        }
        
        final List<DeviceIdentifier> deviceIdList = new ArrayList<>();
        for (FileItem file : files) {
            final String fileName = file.getName();
            final Map<String, DeviceIdentifier> deviceIds = new HashMap<>();
            
            Import.INSTANCE.importFixes(file.getInputStream(), new FixCallback() {
                @Override
                public void addFix(GPSFix fix, String trackName) {
                    DeviceIdentifier deviceId = deviceIds.get(trackName);
                    if (deviceId == null) {
                        deviceId = new TrackFileImportDeviceIdentifierImpl(fileName, trackName);
                        deviceIds.put(trackName, deviceId);
                        deviceIdList.add(deviceId);
                    }
                    storeFix(fix, deviceId);
                }
                
                @Override
                public void addFix(GPSFix fix) {
                    DeviceIdentifier deviceId = deviceIds.get(null);
                    if (deviceId == null) {
                        deviceId = new TrackFileImportDeviceIdentifierImpl(fileName, null);
                        deviceIds.put(null, deviceId);
                        deviceIdList.add(deviceId);
                    }
                    storeFix(fix, deviceId);
                }
            }, true);
        }
        
        //setJsonResponseHeader(resp);
        //DO NOT set a JSON response header. This causes the browser to wrap the response in a
        //<pre> tag when uploading from GWT, as this is an AJAX-request inside an iFrame.
        resp.setContentType("text/html");
        
        TypeBasedServiceFinder<DeviceIdentifierJsonHandler> serviceFinder =
                getServiceFinderFactory().createServiceFinder(DeviceIdentifierJsonHandler.class);
        JsonSerializer<DeviceIdentifier> serializer = new DeviceIdentifierJsonSerializer(serviceFinder);
        JSONArray array = new JSONArray();
        for (DeviceIdentifier deviceId : deviceIdList) {
            array.add(serializer.serialize(deviceId));
        }
        
        array.writeJSONString(resp.getWriter());
    }
}
