package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.shared.events.RegisterCompetitorEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.impl.CompetitorJsonConstants;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.BoatJsonSerializer;

public class RaceLogRegisterCompetitorEventSerializer extends BaseRaceLogEventSerializer {
    public static final String VALUE_CLASS = RegisterCompetitorEvent.class.getSimpleName();
    
    /**
     * The following had been introduced temporarily during the development and initial deployment of
     * bug2822. However, during subsequent cleanup activities the distinction between competitors with
     * and without boat happens through the presence or absence, respectively, of the {@link CompetitorJsonConstants#FIELD_BOAT}
     * field in the JSON document. Hence, a separate field is no longer needed. Only for backward compatibility
     * for the rare case that some installation produced events with this field name does the field need to
     * be preserved.
     */
    public static final String FIELD_COMPETITOR_WITHBOAT = "competitorWithBoat";
    
    public static final String FIELD_COMPETITOR = "competitor";
    
    public static final String FIELD_BOAT = "boat";

    private final BoatJsonSerializer boatSerializer;

    public RaceLogRegisterCompetitorEventSerializer(JsonSerializer<Competitor> competitorSerializer, 
            BoatJsonSerializer boatSerializer) {
        super(competitorSerializer);
        this.boatSerializer = boatSerializer;
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }
    
    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogRegisterCompetitorEvent registerCompetitorEvent = (RaceLogRegisterCompetitorEvent) object;
        JSONObject result = super.serialize(registerCompetitorEvent);
        result.put(FIELD_COMPETITOR, competitorSerializer.serialize(registerCompetitorEvent.getCompetitor()));
        result.put(FIELD_BOAT, boatSerializer.serialize(registerCompetitorEvent.getBoat()));
        return result;
    }
}
