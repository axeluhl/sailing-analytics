package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceIdentifier;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WithID;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogDeviceMappingEventSerializer;

public abstract class RaceLogDeviceMappingEventDeserializer<ItemT extends WithID>
extends BaseRaceLogEventDeserializer {
    protected final JsonDeserializer<DeviceIdentifier> deviceDeserializer;

    public RaceLogDeviceMappingEventDeserializer(
            JsonDeserializer<Competitor> competitorDeserializer,
            JsonDeserializer<DeviceIdentifier> deviceDeserializer) {
        super(competitorDeserializer);
        this.deviceDeserializer = deviceDeserializer;
    }

    protected abstract RaceLogEvent furtherDeserialize(JSONObject itemObject, TimePoint from, TimePoint to,
            DeviceIdentifier device, Serializable id, TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint timePoint, int passId) throws JsonDeserializationException;

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id,
            TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint timePoint, int passId, List<Competitor> competitors)
                    throws JsonDeserializationException {
        JSONObject deviceJson = Helpers.toJSONObjectSafe(object.get(RaceLogDeviceMappingEventSerializer.FIELD_DEVICE));
        DeviceIdentifier device = deviceDeserializer.deserialize(deviceJson);
        JSONObject itemObject = Helpers.getNestedObjectSafe(object, RaceLogDeviceMappingEventSerializer.FIELD_ITEM);
        long fromMillis = (Long) object.get(RaceLogDeviceMappingEventSerializer.FIELD_FROM_MILLIS);
        long toMillis = (Long) object.get(RaceLogDeviceMappingEventSerializer.FIELD_TO_MILLIS);
        TimePoint from = new MillisecondsTimePoint(fromMillis);
        TimePoint to = new MillisecondsTimePoint(toMillis);
        return furtherDeserialize(itemObject, from, to, device, id, createdAt, author, timePoint, passId);
    }
}
