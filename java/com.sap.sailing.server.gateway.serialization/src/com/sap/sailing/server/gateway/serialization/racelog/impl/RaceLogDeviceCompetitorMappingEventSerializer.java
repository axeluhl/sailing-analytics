package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceIdentifier;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogDeviceCompetitorMappingEventSerializer extends RaceLogDeviceMappingEventSerializer<Competitor> {
	public static final String VALUE_CLASS = DeviceCompetitorMappingEvent.class.getSimpleName();

	public RaceLogDeviceCompetitorMappingEventSerializer(
			JsonSerializer<Competitor> competitorSerializer,
			JsonSerializer<DeviceIdentifier> deviceSerializer) {
		super(competitorSerializer, deviceSerializer);
	}
	
    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

	@Override
	protected JSONObject serializeItem(Competitor item) {
		return competitorSerializer.serialize(item);
	}
}
