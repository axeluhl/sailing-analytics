package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sse.common.TimePoint;

public class RaceLogDeviceCompetitorMappingEventDeserializer extends
		RaceLogDeviceMappingEventDeserializer<Competitor> {
	public RaceLogDeviceCompetitorMappingEventDeserializer(
			JsonDeserializer<Competitor> competitorDeserializer,
			JsonDeserializer<DeviceIdentifier> deviceDeserializer) {
		super(competitorDeserializer, deviceDeserializer);
	}

	@Override
	protected RaceLogEvent furtherDeserialize(JSONObject itemObject, TimePoint from, TimePoint to,
			DeviceIdentifier device, Serializable id, TimePoint createdAt,
			AbstractLogEventAuthor author, TimePoint timePoint, int passId) throws JsonDeserializationException {
		Competitor mappedTo = competitorDeserializer.deserialize(itemObject);
		return factory.createDeviceCompetitorMappingEvent(createdAt, author, timePoint, id, device,
				mappedTo, passId, from, to);
	}

}
