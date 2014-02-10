package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.devices.DeviceIdentifier;
import com.sap.sailing.domain.devices.TypeBasedServiceFinder;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.devices.DeviceIdentifierJsonSerializationHandler;

public class RaceLogDeviceCompetitorMappingEventDeserializer extends
		RaceLogDeviceMappingEventDeserializer<Competitor> {
	public RaceLogDeviceCompetitorMappingEventDeserializer(
			JsonDeserializer<Competitor> competitorDeserializer,
			TypeBasedServiceFinder<DeviceIdentifierJsonSerializationHandler> deviceServiceFinder) {
		super(competitorDeserializer, deviceServiceFinder);
	}

	@Override
	protected RaceLogEvent furtherDeserialize(Serializable itemId, TimePoint from, TimePoint to,
			DeviceIdentifier device, Serializable id, TimePoint createdAt,
			RaceLogEventAuthor author, TimePoint timePoint, int passId) {
		Competitor mappedTo = DomainFactory.INSTANCE.getExistingCompetitorById(itemId);
		return factory.createDeviceCompetitorMappingEvent(createdAt, author, timePoint, id, device,
				mappedTo, passId, from, to);
	}

}
