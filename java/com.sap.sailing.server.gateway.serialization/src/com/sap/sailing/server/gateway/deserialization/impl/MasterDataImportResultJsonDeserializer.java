package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.dto.MasterDataImportResultImpl;
import com.sap.sailing.server.gateway.interfaces.MasterDataImportResult;
import com.sap.sailing.server.gateway.serialization.LeaderboardGroupConstants;
import com.sap.sailing.server.gateway.serialization.impl.MasterDataImportResultJsonSerializer;
import com.sap.sse.common.Util;
import com.sap.sse.shared.json.JsonDeserializationException;
import com.sap.sse.shared.json.JsonDeserializer;

public class MasterDataImportResultJsonDeserializer implements JsonDeserializer<MasterDataImportResult> {
    @Override
    public MasterDataImportResult deserialize(JSONObject object) throws JsonDeserializationException {
        final JSONArray leaderboardGroupsImportedWithEventIdsJson = (JSONArray) object.get(MasterDataImportResultJsonSerializer.LEADERBOARDGROUPS_IMPORTED);
        final Set<MasterDataImportResult.LeaderboardGroupWithEventIds> leaderboardGroupsImportedWithEventIds = new HashSet<>();
        for (final Object o : leaderboardGroupsImportedWithEventIdsJson) {
            final JSONObject leaderboardGroupImportedWithEventIdsJson = (JSONObject) o;
            leaderboardGroupsImportedWithEventIds.add(new MasterDataImportResult.LeaderboardGroupWithEventIds() {
                private static final long serialVersionUID = 1L;

                @Override
                public UUID getId() {
                    return UUID.fromString(leaderboardGroupImportedWithEventIdsJson.get(LeaderboardGroupConstants.ID).toString());
                }

                @Override
                public String getName() {
                    return leaderboardGroupImportedWithEventIdsJson.get(LeaderboardGroupConstants.NAME).toString();
                }

                @Override
                public Iterable<UUID> getEventIds() {
                    return Util.map(
                            (JSONArray) leaderboardGroupImportedWithEventIdsJson.get(LeaderboardGroupConstants.EVENTS),
                            eventIdAsString -> UUID.fromString(eventIdAsString.toString()));
                }
            });
        }
        final String remoteServerUrl = object.get(MasterDataImportResultJsonSerializer.IMPORTED_FROM).toString();
        final boolean override = Boolean.valueOf(object.get(MasterDataImportResultJsonSerializer.OVERRIDE_FORM_PARAM).toString());
        final boolean importWind = Boolean.valueOf(object.get(MasterDataImportResultJsonSerializer.EXPORT_WIND_FORM_PARAM).toString());
        final boolean importDeviceConfigurations = Boolean.valueOf(object.get(MasterDataImportResultJsonSerializer.EXPORT_DEVICE_CONFIGS_FORM_PARAM).toString());
        final boolean importTrackedRacesAndStartTracking = Boolean.valueOf(object.get(MasterDataImportResultJsonSerializer.EXPORT_TRACKED_RACES_AND_START_TRACKING_FORM_PARAM).toString());
        return new MasterDataImportResultImpl(leaderboardGroupsImportedWithEventIds, remoteServerUrl, override, importWind, importDeviceConfigurations, importTrackedRacesAndStartTracking);
    }
}
