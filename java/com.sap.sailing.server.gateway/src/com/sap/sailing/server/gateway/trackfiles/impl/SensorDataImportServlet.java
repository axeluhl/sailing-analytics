package com.sap.sailing.server.gateway.trackfiles.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackimport.DoubleVectorFixImporter;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.server.gateway.impl.AbstractFileUploadServlet;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
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
     *            contents; the importer names are matched against {@link DoubleVectorFixImporter#getType()} for
     *            all importers found registered in the OSGi registry. The first matching importer is used for the
     *            file. The importer is selected on a per-file basis.
     * 
     * @throws IOException
     */
    private Iterable<TrackFileImportDeviceIdentifier> importFiles(Iterable<Pair<String, FileItem>> files)
            throws IOException {
        final Set<TrackFileImportDeviceIdentifier> deviceIds = new HashSet<>();
        final Map<DeviceIdentifier, TimePoint> from = new HashMap<>();
        final Map<DeviceIdentifier, TimePoint> to = new HashMap<>();
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
                throw new RuntimeException("Sensor importer not found");
            }
            logger.log(Level.INFO,
                    "Going to import sensor data file  with importer " + importerToUse.getClass().getSimpleName());
            try (BufferedInputStream in = new BufferedInputStream(fi.getInputStream())) {
                try {
                    importerToUse.importFixes(in, new DoubleVectorFixImporter.Callback() {
                        @Override
                        public void addFixes(Iterable<DoubleVectorFix> fixes, TrackFileImportDeviceIdentifier device) {
                            deviceIds.add(device);
                            storeFixes(fixes, device);
                            TimePoint earliestFixSoFarFromCurrentDevice = from.get(device);
                            TimePoint latestFixSoFarFromCurrentDevice = to.get(device);
                            for (DoubleVectorFix fix : fixes) {
                                if (earliestFixSoFarFromCurrentDevice == null
                                        || earliestFixSoFarFromCurrentDevice.after(fix.getTimePoint())) {
                                    earliestFixSoFarFromCurrentDevice = fix.getTimePoint();
                                    from.put(device, earliestFixSoFarFromCurrentDevice);
                                }
                                if (latestFixSoFarFromCurrentDevice == null
                                        || latestFixSoFarFromCurrentDevice.before(fix.getTimePoint())) {
                                    latestFixSoFarFromCurrentDevice = fix.getTimePoint();
                                    to.put(device, latestFixSoFarFromCurrentDevice);
                                }
                            }
                        }
                    }, fi.getName(), requestedImporterName, /* downsample */ true);
                    logger.log(Level.INFO, "Successfully imported file " + requestedImporterName);
                } catch (FormatNotSupportedException e) {
                    logger.log(Level.INFO, "Failed to import file " + requestedImporterName);
                }
            }
        }
        return deviceIds;
    }

    /**
     * Process the uploaded file items.
     */
    @Override
    protected void process(List<FileItem> fileItems, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
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
        final Iterable<TrackFileImportDeviceIdentifier> mappingList = importFiles(filesAndImporterNames);
        resp.setContentType("text/html");
        for (TrackFileImportDeviceIdentifier mapping : mappingList) {
            String stringRep = mapping.getId().toString();
            resp.getWriter().println(stringRep);
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
}
