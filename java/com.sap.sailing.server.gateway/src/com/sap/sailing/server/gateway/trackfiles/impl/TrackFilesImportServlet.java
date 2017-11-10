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
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.trackimport.GPSFixImporter;
import com.sap.sailing.domain.trackimport.GPSFixImporter.Callback;
import com.sap.sailing.server.gateway.impl.AbstractFileUploadServlet;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
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

    public void storeFix(GPSFix fix, DeviceIdentifier deviceIdentifier) {
        try {
            getService().getSensorFixStore().storeFix(deviceIdentifier, fix);
        } catch (NoCorrespondingServiceRegisteredException e) {
            logger.log(Level.WARNING, "Could not store fix for " + deviceIdentifier);
        }
    }
    
    Collection<GPSFixImporter> getGPSFixImporters(String fileExtension) {
        List<GPSFixImporter> result = new ArrayList<>();
        Collection<ServiceReference<GPSFixImporter>> refs;
        try {
            Filter filter = null;
            if (fileExtension != null) {
                filter = getContext().createFilter(String.format("(%s=%s)",
                        GPSFixImporter.FILE_EXTENSION_PROPERTY, fileExtension));
            }
            refs = getContext().getServiceReferences(GPSFixImporter.class, filter == null ? null : filter.toString());
            for (ServiceReference<GPSFixImporter> ref : refs) {
                result.add(getContext().getService(ref));
            }
        } catch (InvalidSyntaxException e) {
            logger.log(Level.WARNING, "Could not create OSGi filter for file extension");
        }
        return result;
    }

    protected Iterable<TrackFileImportDeviceIdentifier> importFiles(Iterable<Pair<String, FileItem>> files,
            GPSFixImporter preferredImporter) throws IOException {
        final Set<TrackFileImportDeviceIdentifier> deviceIds = new HashSet<>();
        final Map<DeviceIdentifier, TimePoint> from = new HashMap<>();
        final Map<DeviceIdentifier, TimePoint> to = new HashMap<>();
        for (Pair<String, FileItem> pair : files) {
            final String fileName = pair.getA();
            final FileItem fileItem = pair.getB();
            String fileExt = null;
            if (fileName.contains(".")) {
                fileExt = fileName.substring(fileName.lastIndexOf(".") + 1);
            }
            
            Set<GPSFixImporter> importersToTry = new LinkedHashSet<>();
            if (preferredImporter != null) {
                importersToTry.add(preferredImporter);
            }
            importersToTry.addAll(getGPSFixImporters(fileExt));
            importersToTry.addAll(getGPSFixImporters(null));

            logger.log(Level.INFO, 
                    "System knows " + 
                            importersToTry.size() + 
                            " importers: "+
                            importersToTry.stream().map(i -> i.getType()).collect(Collectors.joining(", "))
                    );
            
            parsersLoop: for (GPSFixImporter importer : importersToTry) {
                boolean succeeded = false;
                logger.log(Level.INFO, "Trying to import file " + fileName + " with importer " + importer.getType());
                try (BufferedInputStream in = new BufferedInputStream(fileItem.getInputStream())) {
                    try {
                        importer.importFixes(in, new Callback() {
                            @Override
                            public void addFix(GPSFix fix, TrackFileImportDeviceIdentifier device) {
                                deviceIds.add(device);
                                storeFix(fix, device);
                                TimePoint earliestFixSoFarFromCurrentDevice = from.get(device);
                                if (earliestFixSoFarFromCurrentDevice == null
                                        || earliestFixSoFarFromCurrentDevice.after(fix.getTimePoint())) {
                                    earliestFixSoFarFromCurrentDevice = fix.getTimePoint();
                                    from.put(device, earliestFixSoFarFromCurrentDevice);
                                }
                                TimePoint latestFixSoFarFromCurrentDevice = to.get(device);
                                if (latestFixSoFarFromCurrentDevice == null
                                        || latestFixSoFarFromCurrentDevice.before(fix.getTimePoint())) {
                                    latestFixSoFarFromCurrentDevice = fix.getTimePoint();
                                    to.put(device, latestFixSoFarFromCurrentDevice);
                                }
                            }
                        }, true, fileName);
                        succeeded = true;
                    } catch (Exception e) {
                        logger.log(Level.INFO, "Failed with " + e.getClass().getSimpleName()
                                + " while importing file using " + importer.getType());
                    }
                }
                if (succeeded) {
                    logger.log(Level.INFO, "Successfully imported file " + fileName + " using " + importer.getType());
                    break parsersLoop;
                }
            }
        }
        return deviceIds;
    }

    @Override
    protected void process(List<FileItem> fileItems, HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
        GPSFixImporter preferredImporter = null;
        if (prefImporterType != null && !prefImporterType.isEmpty()) {
            preferredImporter = getServiceFinderFactory().createServiceFinder(GPSFixImporter.class)
                    .findService(prefImporterType);
        }
        final Iterable<TrackFileImportDeviceIdentifier> mappingList = importFiles(files, preferredImporter);
        // setJsonResponseHeader(resp);
        // DO NOT set a JSON response header. This causes the browser to wrap the response in a
        // <pre> tag when uploading from GWT, as this is an AJAX-request inside an iFrame.
        resp.setContentType("text/html");
        for (TrackFileImportDeviceIdentifier mapping : mappingList) {
            String stringRep = mapping.getId().toString();
            resp.getWriter().println(stringRep);
        }
    }
}
