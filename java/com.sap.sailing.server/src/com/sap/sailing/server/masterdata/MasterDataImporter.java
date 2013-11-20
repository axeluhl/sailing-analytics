package com.sap.sailing.server.masterdata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.MasterDataImportObjectCreationCount;
import com.sap.sailing.domain.common.impl.MasterDataImportObjectCreationCountImpl;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.masterdataimport.LeaderboardGroupMasterData;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.masterdata.impl.LeaderboardGroupMasterDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.masterdata.impl.MediaTrackJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.TopLevelMasterDataSerializer;
import com.sap.sailing.server.operationaltransformation.ImportMasterDataOperation;

public class MasterDataImporter {
    private final DomainFactory baseDomainFactory;
    
    private final RacingEventService racingEventService;
    
    public MasterDataImporter(DomainFactory baseDomainFactory, RacingEventService racingEventService) {
        this.baseDomainFactory = baseDomainFactory;
        this.racingEventService = racingEventService;
    }

    public MasterDataImportObjectCreationCount importMasterData(String string, boolean override) {
        MasterDataImportObjectCreationCountImpl creationCount = new MasterDataImportObjectCreationCountImpl();
        JsonDeserializer<LeaderboardGroupMasterData> leaderboardGroupMasterDataDeserializer = LeaderboardGroupMasterDataJsonDeserializer.create(baseDomainFactory);
        JSONParser parser = new JSONParser();
        try {
            JSONObject masterDataOverall = (JSONObject) parser.parse(string);
            JSONArray leaderboardGroupsMasterDataJsonArray = (JSONArray) masterDataOverall.get(TopLevelMasterDataSerializer.FIELD_PER_LG);
            for (Object leaderBoardGroupMasterData : leaderboardGroupsMasterDataJsonArray) {
                JSONObject leaderBoardGroupMasterDataJson = (JSONObject) leaderBoardGroupMasterData;
                LeaderboardGroupMasterData masterData = leaderboardGroupMasterDataDeserializer
                        .deserialize(leaderBoardGroupMasterDataJson);
                ImportMasterDataOperation op = new ImportMasterDataOperation(masterData, override, creationCount, baseDomainFactory);
                creationCount = racingEventService.apply(op);
            }
            JsonDeserializer<MediaTrack> mediaTrackDeserializer = new MediaTrackJsonDeserializer();
            JSONArray mediaTracks = (JSONArray) masterDataOverall.get(TopLevelMasterDataSerializer.FIELD_MEDIA);
            List<MediaTrack> tracks = new ArrayList<MediaTrack>();
            for (Object obj : mediaTracks) {
                tracks.add(mediaTrackDeserializer.deserialize((JSONObject) obj));
            }
            Collection<MediaTrack> existingMediaTracks = racingEventService.getAllMediaTracks();
            Map<String, MediaTrack> existingMap = new HashMap<String, MediaTrack>();
            
            for (MediaTrack oneTrack : existingMediaTracks) {
                existingMap.put(oneTrack.dbId, oneTrack);
            }
            
            for (MediaTrack oneNewTrack : tracks) {
                if (existingMap.containsKey(oneNewTrack.dbId)) {
                    if (override) {
                        racingEventService.mediaTrackDeleted(existingMap.get(oneNewTrack.dbId));
                    } else {
                        continue;
                    }
                }
                racingEventService.mediaTrackAdded(oneNewTrack);
            }
        } catch (org.json.simple.parser.ParseException e) {
            throw new RuntimeException(e);
        } catch (JsonDeserializationException e) {
            throw new RuntimeException(e);
        }
        return creationCount;
    }

}
