package com.sap.sailing.server.gateway.serialization.masterdata.impl;

import java.io.Serializable;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceColumnMasterDataJsonSerializer implements JsonSerializer<RaceColumn> {
    
    public static final String FIELD_MEDAL_RACE = "medalRace";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_RACE_IDENTIFIERS = "raceIdentifiers";
    public static final String FIELD_FLEET_NAME = "fleetName";
    public static final String FIELD_RACE_NAME = "raceName";
    public static final String FIELD_REGATTA_NAME = "regattaName";
    public static final String FIELD_FACTOR = "factor";
    public static final String FIELD_WIND = "wind";
    
    @Override
    public JSONObject serialize(RaceColumn raceColumn) {
        JSONObject jsonRaceColumn = new JSONObject();
        jsonRaceColumn.put(FIELD_NAME, raceColumn.getName());
        jsonRaceColumn.put(FIELD_MEDAL_RACE, raceColumn.isMedalRace());
        jsonRaceColumn.put(FIELD_FACTOR, raceColumn.getExplicitFactor());
        JSONArray raceIdentifiers = new JSONArray();
        jsonRaceColumn.put(FIELD_RACE_IDENTIFIERS, raceIdentifiers);
        for (Fleet fleet : raceColumn.getFleets()) {
            RaceIdentifier raceIdentifierForFleet = raceColumn.getRaceIdentifier(fleet);
            if (raceIdentifierForFleet != null) {
                JSONObject jsonRaceIdentifierForFleet = new JSONObject();
                jsonRaceIdentifierForFleet.put(FIELD_FLEET_NAME, fleet.getName());
                jsonRaceIdentifierForFleet.put(FIELD_RACE_NAME, raceIdentifierForFleet.getRaceName());
                jsonRaceIdentifierForFleet.put(FIELD_REGATTA_NAME, raceIdentifierForFleet.getRegattaName());
                raceIdentifiers.add(jsonRaceIdentifierForFleet);
            }
            TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
            if (trackedRace != null) {
                jsonRaceColumn.put(FIELD_WIND, serializeWindInformation(trackedRace));
            }
        }
        return jsonRaceColumn;
    }

    private JSONArray serializeWindInformation(TrackedRace trackedRace) {
        JSONArray jsonWindInfo = new JSONArray();
        Iterable<WindSource> windSources = trackedRace.getWindSources();
        String raceName = trackedRace.getRace().getName();
        Serializable raceId = trackedRace.getRace().getId();
        String regattaName = trackedRace.getTrackedRegatta().getRegatta().getName();
        if (windSources != null) {
            for (WindSource source : windSources) {
                if (source.canBeStored()) {
                    WindTrack track = trackedRace.getOrCreateWindTrack(source);
                    JsonSerializer<WindTrack> windtrackSerializer = new WindTrackMasterDataJsonSerializer(source,
                            regattaName, raceName, raceId);
                    jsonWindInfo.add(windtrackSerializer.serialize(track));
                }
            }
        }
        return jsonWindInfo;
    } 

}
