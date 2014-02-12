package com.sap.sailing.domain.racelog.tracking.test.impl;

import static com.sap.sailing.domain.common.impl.Util.size;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.common.racelog.tracking.NoCorrespondingServiceRegisteredException;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.racelog.tracking.servlet.RecordFixesPostServlet;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockDeviceAndSessioinIdentifierWithGPSFixesDeserializer;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockServiceFinderFactory;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceAndSessionIdentifierWithGPSFixesDeserializer;
import com.sap.sailing.server.impl.RacingEventServiceImpl;

public class RecordFixesTest {
	private RacingEventService service;
	private RecordFixesPostServlet servlet;
	private final DeviceIdentifier device = new SmartphoneImeiIdentifier("a");

	@Before
	public void before() {
		MockServiceFinderFactory serviceFinderFactory = new MockServiceFinderFactory();
		DeviceAndSessionIdentifierWithGPSFixesDeserializer deserializer =
				new MockDeviceAndSessioinIdentifierWithGPSFixesDeserializer();
		service = new RacingEventServiceImpl(null, serviceFinderFactory);
		servlet = new RecordFixesPostServlet();
		servlet = spy(servlet);
		when(servlet.getRequestDeserializer()).thenReturn(deserializer);
		doReturn(service).when(servlet).getService();
	}
	
	@After
	public void after() {
		MongoObjectFactoryImpl mongoOF = (MongoObjectFactoryImpl) service.getMongoObjectFactory();
		mongoOF.getGPSFixCollection().drop();
	}
	
	private GPSFixMoving createFix(long millis, long lat, long lng, long knots, long degrees) {
		return new GPSFixMovingImpl(new DegreePosition(lat, lng),
				new MillisecondsTimePoint(millis), new KnotSpeedWithBearingImpl(knots, new DegreeBearingImpl(degrees)));
	}

	@Test
	public void areFixesStoredInDb() throws TransformationException, NoCorrespondingServiceRegisteredException {
		int timepoint = 343;
		List<GPSFix> fixes = new ArrayList<>();
		fixes.add(createFix(timepoint, 0, 0, 0, 0));
		fixes.add(createFix(timepoint+1, 0, 0, 0, 0));
		Triple<DeviceIdentifier, Serializable, List<GPSFix>> data = new Triple<>(device, null, fixes);
		servlet.process(null, data);
		
		DynamicTrack<GPSFix> track = service.getDomainObjectFactory().loadGPSFixTrack(device);
		track.lockForRead();
		assertEquals(2, size(track.getRawFixes()));
		assertEquals(timepoint, track.getFirstRawFix().getTimePoint().asMillis());
		assertTrue(track.getFirstRawFix() instanceof GPSFixMoving);
		track.unlockAfterRead();
	}
}
