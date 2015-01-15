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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.sap.sailing.domain.common.racelog.tracking.NoCorrespondingServiceRegisteredException;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.trackimport.GPSFixImporter;
import com.sap.sailing.domain.trackimport.GPSFixImporter.Callback;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
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
 * <li>If the type of a {@link #PREFERRED_IMPORTER preferred importer} is transmitted, this is the first that is used.</li>
 * <li>Then the importers registered for a matching {@link GPSFixImporter#FILE_EXTENSION_PROPERTY file extension} are
 * used.</li>
 * <li>If all this fails, all other available importers are used.</li>
 * </ul>
 * 
 * @author Fredrik Teschke
 * 
 */
public class TrackFilesImportServlet extends AbstractJsonHttpServlet {
    public static final String PREFERRED_IMPORTER = "preferredImporter";
    private static final long serialVersionUID = 1120226743039934620L;
    private static final Logger logger = Logger.getLogger(TrackFilesImportServlet.class.getName());
    
    private static final int READ_BUFFER_SIZE = 1024 * 1024 * 1024;

    public void storeFix(GPSFix fix, DeviceIdentifier deviceIdentifier) {
        try {
            getService().getGPSFixStore().storeFix(deviceIdentifier, fix);
        } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
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
    
    Iterable<TrackFileImportDeviceIdentifier> importFiles(Iterable<Pair<String, InputStream>> files, GPSFixImporter preferredImporter)
        throws IOException {
        final Set<TrackFileImportDeviceIdentifier> deviceIds = new HashSet<>();
        final Map<DeviceIdentifier, TimePoint> from = new HashMap<>();
        final Map<DeviceIdentifier, TimePoint> to = new HashMap<>();
        
        for (Pair<String, InputStream> file : files) {
            final String fileName = file.getA();
            String fileExt = null;
            if (fileName.contains(".")) {
                fileExt = fileName.substring(fileName.lastIndexOf(".") + 1);
            }

            Collection<GPSFixImporter> importersToTry = new LinkedHashSet<>();
            if (preferredImporter != null) {
                importersToTry.add(preferredImporter);
            }
            importersToTry.addAll(getGPSFixImporters(fileExt));
            importersToTry.addAll(getGPSFixImporters(null));
            BufferedInputStream in = new BufferedInputStream(file.getB()) {
                @Override
                public void close() throws IOException {
                    //prevent importers from closing this stream
                }
            };
            in.mark(READ_BUFFER_SIZE);
            boolean done = false;
            Iterator<GPSFixImporter> iter = importersToTry.iterator();
            while (iter.hasNext() && ! done) {
                try {
                    in.reset();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                boolean failed = false;
                GPSFixImporter importer = iter.next();
                logger.log(Level.INFO, "Trying to import file " + fileName + " with importer " + importer.getType());
                try {
                    importer.importFixes(in, new Callback() {
                        @Override
                        public void addFix(GPSFix fix, TrackFileImportDeviceIdentifier device) {
                            deviceIds.add(device);
                            storeFix(fix, device);
                            TimePoint earliestFixSoFarFromCurrentDevice = from.get(device);
                            if (earliestFixSoFarFromCurrentDevice == null || earliestFixSoFarFromCurrentDevice.after(fix.getTimePoint())) {
                                earliestFixSoFarFromCurrentDevice = fix.getTimePoint();
                                from.put(device, earliestFixSoFarFromCurrentDevice);
                            }
                            TimePoint latestFixSoFarFromCurrentDevice = to.get(device);
                            if (latestFixSoFarFromCurrentDevice == null || latestFixSoFarFromCurrentDevice.before(fix.getTimePoint())) {
                                latestFixSoFarFromCurrentDevice = fix.getTimePoint();
                                to.put(device, latestFixSoFarFromCurrentDevice);
                            }
                        }

                    }, true, fileName);
                } catch (FormatNotSupportedException e) {
                    failed = true;
                }
                if (! failed) {
                    done = true;
                    logger.log(Level.INFO, "Successfully imported file " + fileName);
                }
            }
        }
        return deviceIds;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!ServletFileUpload.isMultipartContent(req)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Set<Pair<String, InputStream>> files = new HashSet<>();
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items;
        try {
            items = (List<FileItem>) upload.parseRequest(req);
        } catch (FileUploadException e) {
            throw new IOException("Could not parse request");
        }
        
        String prefImporterType = null;
        for (FileItem item : items) {
            if (!item.isFormField())
                files.add(new Pair<String, InputStream>(item.getName(), item.getInputStream()));
            else {
                if (item.getFieldName() != null && item.getFieldName().equals(PREFERRED_IMPORTER)) {
                    prefImporterType = item.getString();
                }
            }
        }
        GPSFixImporter preferredImporter = null;
        if (prefImporterType != null && ! prefImporterType.isEmpty()) {
            preferredImporter = getServiceFinderFactory().createServiceFinder(GPSFixImporter.class).
                    findService(prefImporterType);
        }
        
        final Iterable<TrackFileImportDeviceIdentifier> mappingList = importFiles(files, preferredImporter);
        
        //setJsonResponseHeader(resp);
        //DO NOT set a JSON response header. This causes the browser to wrap the response in a
        //<pre> tag when uploading from GWT, as this is an AJAX-request inside an iFrame.
        resp.setContentType("text/html");
        
        for (TrackFileImportDeviceIdentifier mapping : mappingList) {
            String stringRep = mapping.getId().toString();
            resp.getWriter().println(stringRep);
        }
    }
}
