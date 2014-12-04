package com.sap.sailing.android.tracking.app.test;

import java.util.ArrayList;
import java.util.Date;

import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.test.ServiceTestCase;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TrackingService;
import com.sap.sailing.android.tracking.app.valueobjects.GpsFix;

public class TrackingServiceTest extends ServiceTestCase<TrackingService> {

	static final String TAG = TrackingServiceTest.class.getName();
	final String eventId = "test123";
	long eventRowId;
//	static final String GPS_MOCK_PROVIDER_NAME = "TestProvider";
	
	public TrackingServiceTest() {
		super(TrackingService.class);
	}
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
        DatabaseTestHelper.deleteAllGpsFixesFromDB(getContext());
        DatabaseTestHelper.deleteAllEventsFromDB(getContext());
        eventRowId = DatabaseTestHelper.createNewEventInDBAndReturnItsId(getContext(), "test123", eventId);
//        createGpsTestProvider();
    }
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
//		removeGpsTestProvider();
	}
	
	private void startService()
	{
		Intent startIntent = new Intent();
        startIntent.setClass(getContext(), TrackingService.class);
        startIntent.putExtra(getContext().getString(R.string.tracking_service_event_id_parameter), eventId);
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
     * @throws InterruptedException 
     */
	public void testGPSFixesStoredToDatabase() throws InterruptedException {
		startService();
		
//		LocationManager locationManager = (LocationManager) this.getContext()
//				.getSystemService(Context.LOCATION_SERVICE);
		
		long timestamp = new Date().getTime();
//		long nanos = System.nanoTime();
		
        Location location = new Location("Test");
        location.setLatitude(20.0);
        location.setLongitude(30.0);
        location.setAccuracy(0);
        location.setTime(timestamp);
//        location.setElapsedRealtimeNanos(nanos);
        location.setSpeed(12.5f);
        location.setBearing(123);
        
        getService().onLocationChanged(location);
        
//      locationManager.setTestProviderLocation(GPS_MOCK_PROVIDER_NAME, location);
        
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
	
	
	
//	private void createGpsTestProvider()
//	{
//		LocationManager locationManager = (LocationManager) this.getContext()
//				.getSystemService(Context.LOCATION_SERVICE);
//		
//		if (locationManager.getProvider(GPS_MOCK_PROVIDER_NAME) == null)
//		{
//			locationManager.addTestProvider(GPS_MOCK_PROVIDER_NAME, false, false, false, false, false,
//					false, false, Criteria.POWER_LOW, Criteria.ACCURACY_FINE);	
//		}
//		
//		locationManager.setTestProviderEnabled(GPS_MOCK_PROVIDER_NAME, true);
//	}
//	
//	private void removeGpsTestProvider()
//	{
//		LocationManager locationManager = (LocationManager) this.getContext()
//				.getSystemService(Context.LOCATION_SERVICE);
//		
//		if (locationManager.getProvider(GPS_MOCK_PROVIDER_NAME) != null)
//		{
//			locationManager.removeTestProvider(GPS_MOCK_PROVIDER_NAME);
//		}
//	}
	
	
}
