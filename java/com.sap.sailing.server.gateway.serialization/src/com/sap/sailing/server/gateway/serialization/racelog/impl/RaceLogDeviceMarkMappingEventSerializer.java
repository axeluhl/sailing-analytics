package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.tracking.DeviceIdentifier;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceMarkMappingEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogDeviceMarkMappingEventSerializer extends RaceLogDeviceMappingEventSerializer<Mark> {
	public static final String VALUE_CLASS = DeviceMarkMappingEvent.class.getSimpleName();
	private final JsonSerializer<ControlPoint> markSerializer;
	
	public RaceLogDeviceMarkMappingEventSerializer(
			JsonSerializer<Competitor> competitorSerializer, JsonSerializer<ControlPoint> markSerializer,
			JsonSerializer<DeviceIdentifier> deviceSerializer) {
		super(competitorSerializer, deviceSerializer);
		this.markSerializer = markSerializer;
	}
	
    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

	@Override
	protected JSONObject serializeItem(Mark item) {
		return markSerializer.serialize(item);
	}
}
