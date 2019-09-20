package com.sap.sailing.server.gateway.orc;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.orc.impl.RaceLogORCCertificateAssignmentEventImpl;
import com.sap.sailing.domain.abstractlog.orc.impl.RegattaLogORCCertificateAssignmentEventImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.common.orc.ORCCertificateSelection;
import com.sap.sailing.domain.common.orc.ORCCertificateUploadConstants;
import com.sap.sailing.domain.orc.ORCCertificatesCollection;
import com.sap.sailing.domain.orc.ORCCertificatesImporter;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.deserialization.impl.ORCCertificateSelectionDeserializer;
import com.sap.sailing.server.gateway.impl.AbstractFileUploadServlet;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.security.SessionUtils;

/**
 * Servlet that processes uploaded ORC boat certificate files, can download certificates from URLs and can link
 * certificates to boats in the scope of races and/or regattas. The files obtained by upload or download can be in RMS
 * or JSON format and are probed for either one. A single file may contain multiple certificates. The selection and
 * linking of certificates to boats within the selected context (regatta / race) happens by identifying the certificate
 * by identifying the upload or download resource and within it the sail number, and by providing the boat ID to which
 * to link the certificate.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class ORCCertificateImportServlet extends AbstractFileUploadServlet {
    private static final long serialVersionUID = -1459007826806652976L;
    private static final Logger logger = Logger.getLogger(ORCCertificateImportServlet.class.getName());

    @Override
    protected void process(List<FileItem> fileItems, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String regattaName = null;
        String raceName = null;
        try {
            ORCCertificateSelection certificateSelection = null;
            final Map<String, ORCCertificate> certificates = new HashMap<>();
            for (FileItem item : fileItems) {
                if (item.isFormField()) {
                    if (item.getFieldName() != null) {
                        if (item.getFieldName().equals(ORCCertificateUploadConstants.REGATTA_NAME)) {
                            regattaName = item.getString();
                        } else if (item.getFieldName().equals(ORCCertificateUploadConstants.RACE_NAME)) {
                            raceName = item.getString();
                        } else if (item.getFieldName().equals(ORCCertificateUploadConstants.CERTIFICATE_SELECTION)) {
                            certificateSelection = new ORCCertificateSelectionDeserializer()
                                    .deserialize((JSONObject) new JSONParser().parse(item.getString()));
                        }
                    }
                } else {
                    // file entry: parse certificates from file and key them by their ID
                    final ORCCertificatesCollection certificateCollection = ORCCertificatesImporter.INSTANCE.read(item.getInputStream());
                    for (final ORCCertificate certificate : certificateCollection.getCertificates()) {
                        certificates.put(certificate.getId(), certificate);
                    }
                }
            }
            if (regattaName == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Regatta name ("+ORCCertificateUploadConstants.REGATTA_NAME+") is missing");
            } else if (certificateSelection == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Certificate selection ("+ORCCertificateUploadConstants.CERTIFICATE_SELECTION+") is missing");
            } else {
                if (raceName == null) {
                    createCertificateAssignmentsForRegatta(regattaName, certificateSelection, certificates, resp);
                } else {
                    createCertificateAssignmentsForRace(regattaName, raceName, certificateSelection, certificates, resp);
                }
            }
        } catch (ParseException e) {
            logger.log(Level.INFO, "User "+SessionUtils.getPrincipal()+" was trying to upload a certificate for regatta "+regattaName+", race "+raceName+
                    ", but the certificate mapping failed to parse", e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to parse certificate selection. Probably not valid JSON");
        } finally {
            resp.setContentType("text/html;charset=UTF-8");
        }
    }

    private void createCertificateAssignmentsForRegatta(String regattaName,
            ORCCertificateSelection certificateSelection, Map<String, ORCCertificate> certificates,
            HttpServletResponse resp) throws IOException {
        final Regatta regatta = getService().getRegattaByName(regattaName);
        if (regatta == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Regatta named "+regattaName+" not found");
        } else {
            final RegattaLog regattaLog = regatta.getRegattaLog();
            final LogEventConstructor<RegattaLogEvent, RegattaLogEventVisitor> logEventConstructor = (TimePoint createdAt, TimePoint logicalTimePoint,
                AbstractLogEventAuthor author, Serializable pId, ORCCertificate certificate, Boat boat)->new RegattaLogORCCertificateAssignmentEventImpl(
                        createdAt, logicalTimePoint, author, pId, certificate, boat);
            createCertificateAssignments(regattaLog, logEventConstructor, certificateSelection, certificates, resp);
        }
    }
    
    @FunctionalInterface
    private static interface LogEventConstructor<LogEventT extends AbstractLogEvent<VisitorT>, VisitorT> {
        LogEventT create(TimePoint createdAt, TimePoint logicalTimePoint, AbstractLogEventAuthor author, Serializable pId, ORCCertificate certificate, Boat boat);
    }

    private <LogT extends AbstractLog<LogEventT, VisitorT>, VisitorT, LogEventT extends AbstractLogEvent<VisitorT>> void createCertificateAssignments(
            LogT logToAddTo, LogEventConstructor<LogEventT, VisitorT> logEventConstructor,
            ORCCertificateSelection certificateSelection, Map<String, ORCCertificate> certificates,
            HttpServletResponse resp) {
        final TimePoint now = MillisecondsTimePoint.now();
        final CompetitorAndBoatStore boatStore = getService().getCompetitorAndBoatStore();
        final AbstractLogEventAuthor serverAuthor = getService().getServerAuthor();
        for (final Entry<Serializable, String> mapping : certificateSelection.getCertificateIdsForBoatIds()) {
            final LogEventT assignment = logEventConstructor.create(
                    now, now, serverAuthor, UUID.randomUUID(), certificates.get(mapping.getValue()),
                    boatStore.getExistingBoatById(mapping.getKey()));
            logToAddTo.add(assignment);
        }
    }

    private void createCertificateAssignmentsForRace(String regattaName, String raceName,
            ORCCertificateSelection certificateSelection, Map<String, ORCCertificate> certificates, HttpServletResponse resp) throws IOException {
        final RegattaAndRaceIdentifier raceIdentifier = new RegattaNameAndRaceName(regattaName, raceName);
        final TrackedRace trackedRace = getService().getTrackedRace(raceIdentifier);
        if (trackedRace == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Regatta named "+regattaName+" not found");
        } else {
            final RaceLog raceLog = trackedRace.getAttachedRaceLogs().iterator().next();
            final LogEventConstructor<RaceLogEvent, RaceLogEventVisitor> logEventConstructor = (TimePoint createdAt, TimePoint logicalTimePoint,
                AbstractLogEventAuthor author, Serializable pId, ORCCertificate certificate, Boat boat)->new RaceLogORCCertificateAssignmentEventImpl(
                        createdAt, logicalTimePoint, author, pId, /* passId */ 0, certificate, boat);
            createCertificateAssignments(raceLog, logEventConstructor, certificateSelection, certificates, resp);
        }
    }
}
