package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.shared.events.DeviceMappingEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.WithID;

public abstract class RaceLogDeviceMappingEventSerializer<ItemT extends WithID> extends BaseRaceLogEventSerializer {
    private final JsonSerializer<DeviceIdentifier> deviceSerializer;
    public static final String FIELD_ITEM = "item";
    public static final String FIELD_DEVICE = "device";
    public static final String FIELD_FROM_MILLIS = "fromMillis";
    public static final String FIELD_TO_MILLIS = "toMillis";

    public RaceLogDeviceMappingEventSerializer(JsonSerializer<Competitor> competitorSerializer,
            JsonSerializer<DeviceIdentifier> deviceSerializer) {
        super(competitorSerializer);
        this.deviceSerializer = deviceSerializer;
    }

    protected abstract JSONObject serializeItem(ItemT item);

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        @SuppressWarnings("unchecked")
        DeviceMappingEvent<?, ItemT> event = (DeviceMappingEvent<?, ItemT>) object;
        JSONObject result = super.serialize(object);
        result.put(FIELD_FROM_MILLIS, event.getFrom().asMillis());
        result.put(FIELD_TO_MILLIS, event.getTo().asMillis());
        result.put(FIELD_ITEM, serializeItem(event.getMappedTo()));
        result.put(FIELD_DEVICE, deviceSerializer.serialize(event.getDevice()));
        return result;
    }

}
