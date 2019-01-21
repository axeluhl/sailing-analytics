package com.sap.sailing.windestimation.data.serialization;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.data.WindQuality;

public class RaceWithEstimationDataDeserializer<T> implements JsonDeserializer<RaceWithEstimationData<T>> {

    public static final String COMPETITOR_TRACKS = "competitorTracks";
    public static final String TRACKED_RACE_NAME = "trackedRaceName";
    public static final String REGATTA_NAME = "regattaName";
    public static final String WIND_QUALITY = "windQuality";
    private final CompetitorTrackWithEstimationDataJsonDeserializer<T> competitorTrackWithEstimationDataJsonDeserializer;

    public RaceWithEstimationDataDeserializer(
            CompetitorTrackWithEstimationDataJsonDeserializer<T> competitorTrackWithEstimationDataJsonDeserializer) {
        this.competitorTrackWithEstimationDataJsonDeserializer = competitorTrackWithEstimationDataJsonDeserializer;
    }

    @Override
    public RaceWithEstimationData<T> deserialize(JSONObject object) throws JsonDeserializationException {
        String regattaName = (String) object.get(REGATTA_NAME);
        String raceName = (String) object.get(TRACKED_RACE_NAME);
        WindQuality windQuality = WindQuality.valueOf((String) object.get(WIND_QUALITY));
        JSONArray competitorTracks = (JSONArray) object.get(COMPETITOR_TRACKS);
        List<CompetitorTrackWithEstimationData<T>> competitorTracksWithEstimationData = new ArrayList<>(
                competitorTracks.size());
        for (Object competitorTrackObj : competitorTracks) {
            JSONObject competitorTrack = (JSONObject) competitorTrackObj;
            CompetitorTrackWithEstimationData<T> competitorTrackWithEstimationData = competitorTrackWithEstimationDataJsonDeserializer
                    .deserialize(competitorTrack, regattaName, raceName);
            competitorTracksWithEstimationData.add(competitorTrackWithEstimationData);
        }
        RaceWithEstimationData<T> raceWithEstimationData = new RaceWithEstimationData<>(regattaName, raceName,
                windQuality, competitorTracksWithEstimationData);
        return raceWithEstimationData;
    }

}
