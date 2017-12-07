package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.shared.events.RegisterCompetitorEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.BoatJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorWithBoatJsonSerializer;

public class RaceLogRegisterCompetitorEventSerializer extends BaseRaceLogEventSerializer {
    public static final String VALUE_CLASS = RegisterCompetitorEvent.class.getSimpleName();
    public static final String FIELD_COMPETITOR_WITHBOAT = "competitorWithBoat";
    public static final String FIELD_COMPETITOR = "competitor";
    public static final String FIELD_BOAT = "boat";

    private final CompetitorWithBoatJsonSerializer competitorWithBoatSerializer;
    private final BoatJsonSerializer boatSerializer;

    public RaceLogRegisterCompetitorEventSerializer(JsonSerializer<Competitor> competitorSerializer, 
            CompetitorWithBoatJsonSerializer competitorWithBoatSerializer, BoatJsonSerializer boatSerializer) {
        super(competitorSerializer);
        this.competitorWithBoatSerializer = competitorWithBoatSerializer;
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
        if (registerCompetitorEvent.getCompetitor() instanceof CompetitorWithBoat) {
            CompetitorWithBoat competitorWithBoat =  (CompetitorWithBoat) registerCompetitorEvent.getCompetitor();
            result.put(FIELD_COMPETITOR_WITHBOAT, competitorWithBoatSerializer.serialize(competitorWithBoat));
        } else {
            result.put(FIELD_COMPETITOR, competitorSerializer.serialize(registerCompetitorEvent.getCompetitor()));
            result.put(FIELD_BOAT, boatSerializer.serialize(registerCompetitorEvent.getBoat()));
        }

        return result;
    }
}
