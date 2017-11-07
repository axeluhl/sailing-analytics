package com.sap.sailing.server.gateway.trackfiles.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackimport.DoubleVectorFixImporter;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.server.gateway.impl.AbstractFileUploadServlet;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.Util.Pair;

/**
 * Import servlet for sensor data files. Importers are located through the OSGi service registry and matched against the
 * name provided by the upload form.
 */
public class SensorDataImportServlet extends AbstractFileUploadServlet {
    private static final long serialVersionUID = 1120226743039934620L;
    private static final Logger logger = Logger.getLogger(SensorDataImportServlet.class.getName());

    public void storeFixes(Iterable<DoubleVectorFix> fixes, DeviceIdentifier deviceIdentifier) {
        try {
            getService().getSensorFixStore().storeFixes(deviceIdentifier, fixes);
        } catch (NoCorrespondingServiceRegisteredException e) {
            logger.log(Level.WARNING, "Could not store fix for " + deviceIdentifier);
        }
    }

    /**
     * Searches the requested importer in the importers provided by the OSGi registry and imports the priovided sensor
     * data file.
     * 
     * @param files
     *            the file items together with the names of the importer to use for importing the respective file's
     *            contents; the importer names are matched against {@link DoubleVectorFixImporter#getType()} for all
     *            importers found registered in the OSGi registry. The first matching importer is used for the file. The
     *            importer is selected on a per-file basis.
     * 
     * @throws IOException
     */
    private void importFiles(JsonHolder jsonResult, Iterable<Pair<String, FileItem>> files) throws IOException {
        final Collection<DoubleVectorFixImporter> availableImporters = new LinkedHashSet<>();
        availableImporters.addAll(getOSGiRegisteredImporters());
        for (Pair<String, FileItem> file : files) {
            final String requestedImporterName = file.getA();
            final FileItem fi = file.getB();
            DoubleVectorFixImporter importerToUse = null;
            for (DoubleVectorFixImporter candidate : availableImporters) {
                if (candidate.getType().equals(requestedImporterName)) {
                    importerToUse = candidate;
                    break;
                }
            }
            if (importerToUse == null) {
                throw new RuntimeException("Sensor importer not found: " + requestedImporterName);
            }
            logger.log(Level.INFO,
                    "Start import sensor data file with importer " + importerToUse.getClass().getSimpleName());
            try (BufferedInputStream in = new BufferedInputStream(fi.getInputStream())) {
                final String filename = fi.getName();
                try {
                    importerToUse.importFixes(in, new DoubleVectorFixImporter.Callback() {
                        @Override
                        public void addFixes(Iterable<DoubleVectorFix> fixes, TrackFileImportDeviceIdentifier device) {
                            storeFixes(fixes, device);
                            jsonResult.add(device);
                        }
                    }, filename, requestedImporterName, /* downsample */ true);
                    logger.log(Level.INFO, "Successfully imported file " + requestedImporterName);
                } catch (FormatNotSupportedException e) {
                    jsonResult.add(requestedImporterName, filename, e);
                }
            }
        }
    }

    /**
     * Process the uploaded file items.
     */
    @Override
    protected void process(List<FileItem> fileItems, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        JsonHolder jsonResult = new JsonHolder(logger);
        try {
            String importerName = null;
            searchForPreferredImporter: for (FileItem fi : fileItems) {
                if ("preferredImporter".equalsIgnoreCase(fi.getFieldName())) {
                    importerName = fi.getString();
                    break searchForPreferredImporter;
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
            importFiles(jsonResult, filesAndImporterNames);
        } catch (Exception e) {
            jsonResult.add(e);
        } finally {
            jsonResult.writeJSONString(resp);
        }
    }

    /**
     * Finds all {@link DoubleVectorFixImporter} service references in the OSGi context.
     * 
     * @return
     */
    private Collection<DoubleVectorFixImporter> getOSGiRegisteredImporters() {
        List<DoubleVectorFixImporter> result = new ArrayList<>();
        Collection<ServiceReference<DoubleVectorFixImporter>> refs;
        try {
            refs = getContext().getServiceReferences(DoubleVectorFixImporter.class, null);
            for (ServiceReference<DoubleVectorFixImporter> ref : refs) {
                result.add(getContext().getService(ref));
            }
        } catch (InvalidSyntaxException e) {
            logger.log(Level.WARNING, "Could not create OSGi filter");
        }
        return result;
    }

    public static class JsonHolder {
        private final JSONObject jsonResponseObj = new JSONObject();
        private final JSONArray jsonErrorObj = new JSONArray();
        private final JSONArray jsonUuidObj = new JSONArray();
        private final Logger logger;

        public JsonHolder(Logger logger) {
            this.logger = logger;
            jsonResponseObj.put("errors", jsonErrorObj);
            jsonResponseObj.put("uploads", jsonUuidObj);
        }

        public void add(String requestedImporterName, String filename,
                Exception exception) {
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

        public void add(TrackFileImportDeviceIdentifier mapping) {
            String stringRep = mapping.getId().toString();
            jsonUuidObj.add(stringRep);
        }

        public void writeJSONString(HttpServletResponse resp) throws IOException {
            resp.setContentType("text/html");
            jsonResponseObj.writeJSONString(resp.getWriter());
        }
    }
}
