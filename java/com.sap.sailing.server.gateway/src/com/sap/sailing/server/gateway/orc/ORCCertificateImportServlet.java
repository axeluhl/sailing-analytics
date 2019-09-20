package com.sap.sailing.server.gateway.orc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sailing.domain.common.orc.ORCCertificateSelection;
import com.sap.sailing.domain.common.orc.ORCCertificateUploadConstants;
import com.sap.sailing.server.gateway.deserialization.impl.ORCCertificateSelectionDeserializer;
import com.sap.sailing.server.gateway.impl.AbstractFileUploadServlet;
import com.sap.sse.common.Util.Pair;

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
        try {
            String regattaName = null;
            String raceName = null;
            ORCCertificateSelection certificateSelection;
            List<Pair<String, FileItem>> files = new ArrayList<>();
            for (FileItem item : fileItems) {
                if (!item.isFormField())
                    files.add(new Pair<String, FileItem>(item.getName(), item));
                else {
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
                }
            }
            // setJsonResponseHeader(resp);
            // DO NOT set a JSON response header. This causes the browser to wrap the response in a
            // <pre> tag when uploading from GWT, as this is an AJAX-request inside an iFrame.
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            resp.setContentType("text/html;charset=UTF-8");
        }
    }
}
