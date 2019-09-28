package com.sap.sailing.server.gateway.orc;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.common.orc.ORCCertificateUploadConstants;
import com.sap.sailing.domain.orc.ORCCertificatesCollection;
import com.sap.sailing.domain.orc.ORCCertificatesImporter;
import com.sap.sailing.server.gateway.impl.AbstractFileUploadServlet;
import com.sap.sailing.server.gateway.serialization.racelog.impl.ORCCertificateJsonSerializer;
import com.sap.sse.common.Util;
import com.sap.sse.security.SessionUtils;
import com.sap.sse.util.HttpUrlConnectionHelper;

/**
 * Servlet that processes uploaded ORC boat certificate files, can download certificates from URLs and can link
 * certificates to boats in the scope of races and/or regattas. The files obtained by upload or download can be in RMS
 * or JSON format and are probed for either one. A single file may contain multiple certificates. The selection and
 * linking of certificates to boats within the selected context (regatta / race) happens by identifying the certificate
 * by identifying the upload or download resource and within it the sail number, and by providing the boat ID to which
 * to link the certificate.
 * <p>
 * 
 * Instead of or in addition to uploading certificate files, such documents can also be referenced as URLs by providing
 * zero or more {@link ORCCertificateUploadConstants#CERTIFICATE_URLS} parameters that hold (obviously URL-encoded) URLs
 * pointing to certificate documents in RMS or JSON format.
 * <p>
 * 
 * The context in which to assign the certificates can either be a {@link RegattaLog} or a {@link RaceLog}. The
 * {@link RegattaLog} context can be identified by either providing the
 * {@link ORCCertificateUploadConstants#LEADERBOARD} or the {@link ORCCertificateUploadConstants#REGATTA} parameter. A
 * {@link RaceLog} can be identified either by the triple {@link ORCCertificateUploadConstants#LEADERBOARD} /
 * {@link ORCCertificateUploadConstants#RACE_COLUMN} / {@link ORCCertificateUploadConstants#FLEET} or by the combination
 * {@link ORCCertificateUploadConstants#REGATTA} / {@link ORCCertificateUploadConstants#RACE}.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class ORCCertificateImportServlet extends AbstractFileUploadServlet {
    private static final long serialVersionUID = -1459007826806652976L;
    private static final Logger logger = Logger.getLogger(ORCCertificateImportServlet.class.getName());

    @Override
    protected void process(List<FileItem> fileItems, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            final Set<ORCCertificate> certificates = new HashSet<>();
            for (FileItem item : fileItems) {
                if (item.isFormField()) {
                    if (item.getFieldName() != null) {
                        if (item.getFieldName().equals(ORCCertificateUploadConstants.CERTIFICATE_URLS)) {
                            Util.addAll(parseCertificatesFromUrl(item.getString()), certificates);
                        }
                    }
                } else {
                    // file entry: parse certificates from file and key them by their ID
                    final ORCCertificatesCollection certificateCollection = ORCCertificatesImporter.INSTANCE.read(item.getInputStream());
                    for (final ORCCertificate certificate : certificateCollection.getCertificates()) {
                        certificates.add(certificate);
                    }
                }
            }
            writeResponse(certificates, resp);
        } catch (ParseException e) {
            logger.log(Level.INFO, "User "+SessionUtils.getPrincipal()+" was trying to analyze ORC certificates, but the certificates failed to parse", e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to parse certificate selection. Probably not valid JSON");
        } finally {
            resp.setContentType("text/html;charset=UTF-8");
        }
    }

    private Iterable<ORCCertificate> parseCertificatesFromUrl(String urlAsString) throws IOException, ParseException {
        final URL url = new URL(urlAsString);
        final ORCCertificatesCollection certificates = ORCCertificatesImporter.INSTANCE.read(HttpUrlConnectionHelper.redirectConnection(url).getInputStream());
        final Set<ORCCertificate> result = new HashSet<>();
        for (final ORCCertificate certificate : certificates.getCertificates()) {
            result.add(certificate);
        }
        return result;
    }

    private void writeResponse(Iterable<ORCCertificate> certificates, HttpServletResponse resp) throws IOException {
        // Use text/html to prevent browsers from wrapping the response body,
        // see "Handling File Upload Responses in GWT" at http://www.artofsolving.com/node/50
        resp.setContentType("text/html;charset=UTF-8");
        final JSONObject jsonResponse = new JSONObject();
        final ORCCertificateJsonSerializer certificateSerializer = new ORCCertificateJsonSerializer();
        final JSONArray certificatesAsJson = new JSONArray();
        for (final ORCCertificate certificate : certificates) {
            certificatesAsJson.add(certificateSerializer.serialize(certificate));
        }
        jsonResponse.put(ORCCertificateUploadConstants.CERTIFICATES, certificatesAsJson);
        resp.getWriter().write(jsonResponse.toJSONString());
    }

}
