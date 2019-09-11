package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.orc.impl.RaceLogORCCertificateAssignmentEventImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogORCCertificateAssignmentEventSerializer;
import com.sap.sse.common.TimePoint;

/**
 * Deserializer for {@link com.sap.sailing.domain.abstractlog.orc.RaceLogORCLegDataEvent ORCLegDataEvent}.
 * 
 * @author Daniel Lisunkin (I505543)
 * @author Axel Uhl (d043530)
 */
public class RaceLogORCCertificateAssignmentEventDeserializer extends BaseRaceLogEventDeserializer {

    public RaceLogORCCertificateAssignmentEventDeserializer(JsonDeserializer<DynamicCompetitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt,
            AbstractLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException {
        final ORCCertificate certificate = new ORCCertificateJsonDeserializer().deserialize((JSONObject) object.get(RaceLogORCCertificateAssignmentEventSerializer.ORC_CERTIFICATE));
        final Serializable boatId = (Serializable) object.get(RaceLogORCCertificateAssignmentEventSerializer.ORC_BOAT_ID);
        return new RaceLogORCCertificateAssignmentEventImpl(createdAt, timePoint, author, id, passId, certificate, boatId);
    }

}
