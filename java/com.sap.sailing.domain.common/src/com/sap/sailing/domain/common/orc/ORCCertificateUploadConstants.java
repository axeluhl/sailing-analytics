package com.sap.sailing.domain.common.orc;

/**
 * Constants for parameters and response document fields for the {@link ORCCertificateImportServlet}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface ORCCertificateUploadConstants {
    enum MappingResultStatus { OK, BOAT_NOT_FOUND, CERTIFICATE_NOT_FOUND };

    String REGATTA = "regatta";
    String RACE = "race";
    String LEADERBOARD = "leaderboard";
    String RACE_COLUMN = "race_column";
    String FLEET = "fleet";
    String BOAT_ID = "boat_id";
    String CERTIFICATE_SELECTION = "certificate_selection";
    String CERTIFICATE_ID = "certificate_id";
    String CERTIFICATE_URLS = "certificate_url[]";
    String MAPPINGS = "mappings";
    String CERTIFICATES = "certificates";
    String STATUS = "status";
}
