package com.sap.sailing.server.gateway.deserialization.masterdata.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.LeaderboardMasterData;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.impl.DynamicPerson;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.masterdataimport.EventMasterData;
import com.sap.sailing.domain.masterdataimport.LeaderboardGroupMasterData;
import com.sap.sailing.domain.masterdataimport.RaceColumnMasterData;
import com.sap.sailing.domain.masterdataimport.RegattaMasterData;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.BoatClassJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.ColorDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.FleetDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.NationalityJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.PersonJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.RegattaConfigurationJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.TeamJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogEventDeserializer;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.LeaderboardGroupMasterDataJsonSerializer;

public class LeaderboardGroupMasterDataJsonDeserializer implements JsonDeserializer<LeaderboardGroupMasterData> {
    
    private final JsonDeserializer<LeaderboardMasterData> leaderboardDeserializer;
    
    private final JsonDeserializer<EventMasterData> eventDeserializer;
    
    private final JsonDeserializer<RegattaMasterData> regattaDeserializer;
    
    private final DomainFactory domainFactory;
    
    public static JsonDeserializer<LeaderboardGroupMasterData> create(DomainFactory domainFactory) {
        JsonDeserializer<BoatClass> boatClassDeserializer = new BoatClassJsonDeserializer(domainFactory);
        JsonDeserializer<Nationality> nationalityDeserializer = new NationalityJsonDeserializer(domainFactory);
        JsonDeserializer<DynamicPerson> personDeserializer = new PersonJsonDeserializer(nationalityDeserializer);
        JsonDeserializer<DynamicTeam> teamDeserializer = new TeamJsonDeserializer(personDeserializer);
        JsonDeserializer<Competitor> competitorDeserializer = new CompetitorMasterDataDeserializer(
                boatClassDeserializer, teamDeserializer, domainFactory);
        JsonDeserializer<RaceLogEvent> raceLogEventDeserializer = RaceLogEventDeserializer.create(domainFactory);
        JsonDeserializer<LeaderboardMasterData> leaderboardDeserializer = new LeaderboardMasterDataJsonDeserializer(
                competitorDeserializer, domainFactory, raceLogEventDeserializer);
        JsonDeserializer<EventMasterData> eventDeserializer = new EventMasterDataJsonDeserializer();
        JsonDeserializer<Color> colorDeserializer = new ColorDeserializer();
        JsonDeserializer<Fleet> fleetDeserializer = new FleetDeserializer(colorDeserializer);
        JsonDeserializer<RegattaConfiguration> configurationDeserializer = RegattaConfigurationJsonDeserializer.create();
        JsonDeserializer<RaceColumnMasterData> raceColumnDeserializer = new RaceColumnMasterDataJsonDeserializer();
        JsonDeserializer<RegattaMasterData> regattaDeserializer = new RegattaMasterDataJsonDeserializer(
                fleetDeserializer, raceColumnDeserializer, configurationDeserializer);
        JsonDeserializer<LeaderboardGroupMasterData> leaderboardGroupMasterDataDeserializer = new LeaderboardGroupMasterDataJsonDeserializer(
                leaderboardDeserializer, eventDeserializer, regattaDeserializer, domainFactory);
        return leaderboardGroupMasterDataDeserializer;
    }

    public LeaderboardGroupMasterDataJsonDeserializer(JsonDeserializer<LeaderboardMasterData> leaderboardDeserializer,
            JsonDeserializer<EventMasterData> eventDeserializer, JsonDeserializer<RegattaMasterData> regattaDeserializer, DomainFactory domainFactory) {
        this.leaderboardDeserializer = leaderboardDeserializer;
        this.eventDeserializer = eventDeserializer;
        this.regattaDeserializer = regattaDeserializer;
        this.domainFactory = domainFactory;
    }

    @Override
    public LeaderboardGroupMasterData deserialize(JSONObject object) throws JsonDeserializationException {
        List<LeaderboardMasterData> leaderboards = new ArrayList<LeaderboardMasterData>();
        JSONArray leaderboardsJson = (JSONArray) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_LEADERBOARDS);
        for (Object leaderboardObject : leaderboardsJson) {
            JSONObject leaderboardJson = (JSONObject) leaderboardObject;
            leaderboards.add(leaderboardDeserializer.deserialize(leaderboardJson));
        }
        JSONArray eventsJson = (JSONArray) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_EVENTS);
        Set<EventMasterData> events = new HashSet<EventMasterData>();
        for (Object eventObject : eventsJson) {
            JSONObject eventJson = (JSONObject) eventObject;
            events.add(eventDeserializer.deserialize(eventJson));
        }
        JSONArray regattasJson = (JSONArray) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_REGATTAS);
        Set<RegattaMasterData> regattas = new HashSet<RegattaMasterData>();
        for (Object regattaObject : regattasJson) {
            JSONObject regattaJson = (JSONObject) regattaObject;
            regattas.add(regattaDeserializer.deserialize(regattaJson));
        }
        String name = (String) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_NAME);
        String description = (String) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_DESCRIPTION);
        boolean displayGroupsReverse = (Boolean) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_DISPLAY_GROUPS_REVERSE);
        Boolean hasOverallLeaderboard = (Boolean) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_HAS_OVERALL_LEADERBOARD);
        if (hasOverallLeaderboard == null) {
            hasOverallLeaderboard = false;
        }
        ScoringScheme overallLeaderboardScoringScheme = !hasOverallLeaderboard ? null : LeaderboardMasterDataJsonDeserializer.deserializeScoringScheme((JSONObject) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_OVERALL_LEADERBOARD_SCORING_SCHEME), domainFactory);
        int[] overallLeaderboardResultDiscardingThresholds = !hasOverallLeaderboard ? null : LeaderboardMasterDataJsonDeserializer.deserializeResultDesicardingRule((JSONObject) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_OVERALL_LEADERBOARD_DISCARDING_THRESHOLDS));
        Map<String, Double> metaColumnsWithFactors = !hasOverallLeaderboard ? null : deserializeMetaColumnsWithFactor((JSONArray) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_OVERALL_LEADERBOARD_META_COLUMNS));
        List<String> overallLeaderboardSuppressedCompetitorsIds = !hasOverallLeaderboard ? null : LeaderboardMasterDataJsonDeserializer.deserializeSuppressedCompetitors((JSONArray) object.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_OVERALL_LEADERBOARD_SUPPRESSED_COMPETITORS));
        return new LeaderboardGroupMasterData(name, description, displayGroupsReverse, hasOverallLeaderboard, overallLeaderboardScoringScheme, overallLeaderboardResultDiscardingThresholds, overallLeaderboardSuppressedCompetitorsIds, metaColumnsWithFactors, leaderboards, events, regattas);
    }

    private Map<String, Double> deserializeMetaColumnsWithFactor(JSONArray jsonArray) {
        Map<String, Double> metaColumnsWithFactors = new HashMap<String, Double>();
        if (jsonArray != null) {
            for (Object obj : jsonArray) {
                JSONObject jsonObj = (JSONObject) obj;
                String name = (String) jsonObj.get(LeaderboardGroupMasterDataJsonSerializer.FIELD_NAME);
                Double explicitFactor = (Double) jsonObj
                        .get(LeaderboardGroupMasterDataJsonSerializer.FIELD_EXPLICIT_FACTOR);
                metaColumnsWithFactors.put(name, explicitFactor);
            }
        }
        return metaColumnsWithFactors;
    }

}
