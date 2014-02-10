package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.WithID;
import com.sap.sailing.domain.devices.TypeBasedServiceFinder;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceMappingEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.devices.DeviceIdentifierJsonSerializationHandler;

public abstract class RaceLogDeviceMappingEventSerializer<ItemT extends WithID> extends BaseRaceLogEventSerializer {
	private final TypeBasedServiceFinder<DeviceIdentifierJsonSerializationHandler> deviceServiceFinder;
    public static final String FIELD_ITEM = "item";
    public static final String FIELD_DEVICE_ID = "deviceId";
    public static final String FIELD_DEVICE_TYPE = "deviceType";
    public static final String FIELD_FROM_MILLIS = "fromMillis";
    public static final String FIELD_TO_MILLIS = "toMillis";

    public RaceLogDeviceMappingEventSerializer(JsonSerializer<Competitor> competitorSerializer,
    		TypeBasedServiceFinder<DeviceIdentifierJsonSerializationHandler> deviceServiceFinder) {
        super(competitorSerializer);
        this.deviceServiceFinder = deviceServiceFinder;
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        @SuppressWarnings("unchecked")
		DeviceMappingEvent<ItemT> event = (DeviceMappingEvent<ItemT>) object;
        JSONObject result = super.serialize(event);
        String deviceType = event.getDevice().getIdentifierType();
        result.put(FIELD_ITEM, event.getMappedTo().getId());
        result.put(FIELD_DEVICE_TYPE, deviceType);        
        result.put(FIELD_DEVICE_ID, deviceServiceFinder.findService(deviceType).serialize(event.getDevice()));
        return result;
    }

}
