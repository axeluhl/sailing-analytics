package com.sap.sailing.server.gateway.trackfiles.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
 * The servlet just uses the available importers. Once we start consuming different files, we need to implement the
 * differentiation here.
 */
public class SensorDataImportServlet extends AbstractFileUploadServlet {
    private static final long serialVersionUID = 1120226743039934620L;
    private static final Logger logger = Logger.getLogger(SensorDataImportServlet.class.getName());
    private static final int READ_BUFFER_SIZE = 1024 * 1024 * 1024;

    public void storeFix(DoubleVectorFix fix, DeviceIdentifier deviceIdentifier) {
        try {
            getService().getSensorFixStore().storeFix(deviceIdentifier, fix);
        } catch (NoCorrespondingServiceRegisteredException e) {
            logger.log(Level.WARNING, "Could not store fix for " + deviceIdentifier);
        }
    }

    private Iterable<TrackFileImportDeviceIdentifier> importFiles(Iterable<Pair<String, InputStream>> files)
            throws IOException {
        final Set<TrackFileImportDeviceIdentifier> deviceIds = new HashSet<>();
        final Map<DeviceIdentifier, TimePoint> from = new HashMap<>();
        final Map<DeviceIdentifier, TimePoint> to = new HashMap<>();
        for (Pair<String, InputStream> file : files) {
            final String fileName = file.getA();

            Collection<DoubleVectorFixImporter> importersToTry = new LinkedHashSet<>();

            importersToTry.addAll(getOSGiRegisteredImporters());
            BufferedInputStream in = new BufferedInputStream(file.getB()) {
                @Override
                public void close() throws IOException {
                    // prevent importers from closing this stream
                }
            };
            in.mark(READ_BUFFER_SIZE);
            boolean done = false;
            Iterator<DoubleVectorFixImporter> iter = importersToTry.iterator();
            while (iter.hasNext() && !done) {
                try {
                    in.reset();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                boolean failed = false;
                DoubleVectorFixImporter importer = iter.next();
                logger.log(Level.INFO, "Trying to import file " + fileName + " with importer "
                        + importer.getClass().getSimpleName());
                try {

                    importer.importFixes(in, new DoubleVectorFixImporter.Callback() {
                        @Override
                        public void addFix(DoubleVectorFix fix, TrackFileImportDeviceIdentifier device) {
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
                    }, fileName);
                } catch (FormatNotSupportedException e) {
                    failed = true;
                }
                if (!failed) {
                    done = true;
                    logger.log(Level.INFO, "Successfully imported file " + fileName);
                }
            }
        }
        return deviceIds;
    }

    @Override
    protected void process(List<FileItem> fileItems, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        List<Pair<String, InputStream>> files = new ArrayList<>();

        String importerName = null;
        searchForPrefferedImporter: for (FileItem fi : fileItems) {
            if ("preferredImporter".equalsIgnoreCase(fi.getFieldName())) {
                importerName = fi.getString();
                break searchForPrefferedImporter;
            }
        }
        if (importerName == null) {
            importerName = "BRAVO";
        }
        for (FileItem fi : fileItems) {
            if ("file".equalsIgnoreCase(fi.getFieldName())) {
                files.add(new Pair<>(importerName, fi.getInputStream()));
            }
        }
        final Iterable<TrackFileImportDeviceIdentifier> mappingList = importFiles(files);
        resp.setContentType("text/html");
        for (TrackFileImportDeviceIdentifier mapping : mappingList) {
            String stringRep = mapping.getId().toString();
            resp.getWriter().println(stringRep);
        }
    }

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
