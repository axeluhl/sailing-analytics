package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.orc.RaceLogORCCertificateAssignmentEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

/**
 * Serializer for {@link com.sap.sailing.domain.abstractlog.orc.RaceLogORCLegDataEvent ORCLegDataEvent}.
 * 
 * @author Daniel Lisunkin (i505543)
 * @author Axel Uhl (d043530)
 */
public class RaceLogORCCertificateAssignmentEventSerializer extends BaseRaceLogEventSerializer {

    public static final String VALUE_CLASS = RaceLogORCCertificateAssignmentEvent.class.getSimpleName();
    public static final String ORC_CERTIFICATE = "certificate";
    public static final String ORC_BOAT_ID = "boatId";
    
    public RaceLogORCCertificateAssignmentEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogORCCertificateAssignmentEvent certificateAssignmentEvent = (RaceLogORCCertificateAssignmentEvent) object;
        JSONObject result = super.serialize(certificateAssignmentEvent);
        result.put(ORC_CERTIFICATE, new ORCCertificateJsonSerializer().serialize(certificateAssignmentEvent.getCertificate()));
        result.put(ORC_BOAT_ID, certificateAssignmentEvent.getBoatId());
        return result;
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }
}
