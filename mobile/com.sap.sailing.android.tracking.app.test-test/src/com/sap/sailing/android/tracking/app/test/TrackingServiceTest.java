package com.sap.sailing.android.tracking.app.test;

import java.util.ArrayList;
import java.util.Date;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.test.ServiceTestCase;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TrackingService;
import com.sap.sailing.android.tracking.app.services.TrackingService.GPSQuality;
import com.sap.sailing.android.tracking.app.services.TrackingService.GPSQualityListener;
import com.sap.sailing.android.tracking.app.test.extensions.ServiceHelperTestable;
import com.sap.sailing.android.tracking.app.valueobjects.GpsFix;

public class TrackingServiceTest extends ServiceTestCase<TrackingService> {

	static final String TAG = TrackingServiceTest.class.getName();
	final String eventId = "test123";
	long eventRowId;

	private ServiceHelperTestable serviceHelperSpy;

	public TrackingServiceTest() {
		super(TrackingService.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("dexmaker.dexcache", getContext().getCacheDir().toString());
		DatabaseTestHelper.deleteAllGpsFixesFromDB(getContext());
		DatabaseTestHelper.deleteAllEventsFromDB(getContext());
		eventRowId = DatabaseTestHelper.createNewEventInDBAndReturnItsId(getContext(), "test123",
				eventId);

		serviceHelperSpy = Mockito.mock(ServiceHelperTestable.class);
		ServiceHelperTestable.injectInstance(serviceHelperSpy);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private void startService() {
		Intent startIntent = new Intent();
		startIntent.setClass(getContext(), TrackingService.class);
		startIntent.putExtra(getContext().getString(R.string.tracking_service_event_id_parameter),
				eventId);
		startIntent.setAction(getContext().getString(R.string.tracking_service_start));
		startService(startIntent);
	}

	/**
	 * Test basic startup/shutdown of Service
	 */
	public void testStartable() {
		startService();
		TrackingService service = getService();
		assertNotNull(service);
	}

	/**
	 * Test binding to service
	 */
	public void testBindable() {
		Intent startIntent = new Intent();
		startIntent.setClass(getContext(), TrackingService.class);
		IBinder service = bindService(startIntent);
		assertNotNull(service);
	}

	/**
	 * Test that location updates are stored into DB.
	 * 
	 * @throws InterruptedException
	 */
	public void testGPSFixesStoredToDatabase() throws InterruptedException {
		startService();
		long timestamp = new Date().getTime();

		Location location = new Location("Test");
		location.setLatitude(20.0);
		location.setLongitude(30.0);
		location.setAccuracy(0);
		location.setTime(timestamp);
		location.setSpeed(12.5f);
		location.setBearing(123);

		getService().onLocationChanged(location);

		ArrayList<GpsFix> fixes = DatabaseTestHelper.getAllGpsFixesFromDB(getContext());
		assertEquals(1, fixes.size());

		GpsFix fix = fixes.get(0);

		assertEquals(20.0, fix.latitude);
		assertEquals(30.0, fix.longitude);
		assertEquals(timestamp, fix.timestamp);
		assertEquals(123.0, fix.course);
		assertEquals(12.5, fix.speed);
		assertEquals(0, fix.synced);
		assertEquals(String.valueOf(eventRowId), fix.eventId);

		shutdownService();
	}

	/**
	 * ================ TEST GPS QUALITY ================
	 */

	public void testReportsGpsQualityGreat() {
		MockGpsListener listener = Mockito.spy(new MockGpsListener());
		ArgumentCaptor<GPSQuality> gpsQualityCaptor = ArgumentCaptor.forClass(GPSQuality.class);
		ArgumentCaptor<Float> accuracyCaptor = ArgumentCaptor.forClass(Float.class);
		ArgumentCaptor<Float> bearingCaptor = ArgumentCaptor.forClass(Float.class);
		ArgumentCaptor<Float> speedCaptor = ArgumentCaptor.forClass(Float.class);

		startService();
		TrackingService service = getService();
		service.registerGPSQualityListener(listener);

		getService().onLocationChanged(getTestLocation(2, 12.5f, 123));

		Mockito.verify(listener, Mockito.times(1)).gpsQualityAndAccurracyUpdated(
				gpsQualityCaptor.capture(), accuracyCaptor.capture(), bearingCaptor.capture(),
				speedCaptor.capture());

		assertEquals(GPSQuality.great, gpsQualityCaptor.getValue());
		assertEquals(2f, accuracyCaptor.getValue().floatValue());
		assertEquals(123f, bearingCaptor.getValue().floatValue());
		assertEquals(12.5f, speedCaptor.getValue().floatValue());

		shutdownService();
	}

	public void testReportsGpsQualityGood() {
		MockGpsListener listener = Mockito.spy(new MockGpsListener());
		ArgumentCaptor<GPSQuality> gpsQualityCaptor = ArgumentCaptor.forClass(GPSQuality.class);
		ArgumentCaptor<Float> accuracyCaptor = ArgumentCaptor.forClass(Float.class);
		ArgumentCaptor<Float> bearingCaptor = ArgumentCaptor.forClass(Float.class);
		ArgumentCaptor<Float> speedCaptor = ArgumentCaptor.forClass(Float.class);

		startService();
		TrackingService service = getService();
		service.registerGPSQualityListener(listener);

		getService().onLocationChanged(getTestLocation(50, 12.5f, 123));

		Mockito.verify(listener, Mockito.times(1)).gpsQualityAndAccurracyUpdated(
				gpsQualityCaptor.capture(), accuracyCaptor.capture(), bearingCaptor.capture(),
				speedCaptor.capture());

		assertEquals(GPSQuality.good, gpsQualityCaptor.getValue());
		assertEquals(50f, accuracyCaptor.getValue().floatValue());
		assertEquals(123f, bearingCaptor.getValue().floatValue());
		assertEquals(12.5f, speedCaptor.getValue().floatValue());

		shutdownService();
	}

	public void testReportsGpsQualityPoor() {
		MockGpsListener listener = Mockito.spy(new MockGpsListener());
		ArgumentCaptor<GPSQuality> gpsQualityCaptor = ArgumentCaptor.forClass(GPSQuality.class);
		ArgumentCaptor<Float> accuracyCaptor = ArgumentCaptor.forClass(Float.class);
		ArgumentCaptor<Float> bearingCaptor = ArgumentCaptor.forClass(Float.class);
		ArgumentCaptor<Float> speedCaptor = ArgumentCaptor.forClass(Float.class);

		startService();
		TrackingService service = getService();
		service.registerGPSQualityListener(listener);

		getService().onLocationChanged(getTestLocation(200, 12.5f, 123));

		Mockito.verify(listener, Mockito.times(1)).gpsQualityAndAccurracyUpdated(
				gpsQualityCaptor.capture(), accuracyCaptor.capture(), bearingCaptor.capture(),
				speedCaptor.capture());

		assertEquals(GPSQuality.poor, gpsQualityCaptor.getValue());
		assertEquals(200f, accuracyCaptor.getValue().floatValue());
		assertEquals(123f, bearingCaptor.getValue().floatValue());
		assertEquals(12.5f, speedCaptor.getValue().floatValue());

		shutdownService();
	}
	
	/**
	 * ======= ENSURE TRANSMITTING SERVICE IS STARTED =======
	 */
	
	public void testStartsTransmittingService() {
		MockGpsListener listener = Mockito.spy(new MockGpsListener());
		
		startService();
		TrackingService service = getService();
		service.registerGPSQualityListener(listener);

		getService().onLocationChanged(getTestLocation(1, 2f, 3));
		
		Mockito.verify(serviceHelperSpy, Mockito.times(1)).startTransmittingService(Mockito.any(Context.class));
		
		shutdownService();
	}

	/**
	 * ======= HELPERS =======
	 */

	private Location getTestLocation(float accurracy, float speed, float bearing) {
		long timestamp = new Date().getTime();
		Location location = new Location("Test");
		location.setLatitude(20.0);
		location.setLongitude(30.0);
		location.setAccuracy(accurracy);
		location.setTime(timestamp);
		location.setSpeed(speed);
		location.setBearing(bearing);
		return location;
	}

	public class MockGpsListener implements GPSQualityListener {
		@Override
		public void gpsQualityAndAccurracyUpdated(GPSQuality quality, float gpsAccurracy,
				float gpsBearing, float gpsSpeed) {
		}
	}
}
