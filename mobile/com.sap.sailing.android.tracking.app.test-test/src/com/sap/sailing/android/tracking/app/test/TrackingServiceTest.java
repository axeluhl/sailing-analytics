package com.sap.sailing.android.tracking.app.test;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.IBinder;
import android.test.ServiceTestCase;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Event;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.EventGpsFixesJoined;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.SensorGps;
import com.sap.sailing.android.tracking.app.services.GpsFix;
import com.sap.sailing.android.tracking.app.services.TrackingService;

public class TrackingServiceTest extends ServiceTestCase<TrackingService> {

	static final String TAG = TrackingServiceTest.class.getName();
	static final String GPS_MOCK_PROVIDER_NAME = "TestProvider";
	
	public TrackingServiceTest() {
		super(TrackingService.class);
	}
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllGpsFixesFromDB();
//        createGpsTestProvider();
    }
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
//		removeGpsTestProvider();
	}
	
	private void startService(int eventId)
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
    	startService(123);
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
		startService(998);
		
//		LocationManager locationManager = (LocationManager) this.getContext()
//				.getSystemService(Context.LOCATION_SERVICE);
		
		long timestamp = new Date().getTime();
		long nanos = System.nanoTime();
		
        Location location = new Location(GPS_MOCK_PROVIDER_NAME);
        location.setLatitude(20.0);
        location.setLongitude(30.0);
        location.setAccuracy(0);
        location.setTime(timestamp);
        location.setElapsedRealtimeNanos(nanos);
        location.setSpeed(12.5f);
        location.setBearing(123);
        
        getService().onLocationChanged(location);
        
//      locationManager.setTestProviderLocation(GPS_MOCK_PROVIDER_NAME, location);
        
        ArrayList<GpsFix> fixes = getAllGpsFixesFromDB();
        assertEquals(1, fixes.size());
        
        GpsFix fix = fixes.get(0);
        
        assertEquals(20.0, fix.latitude);
        assertEquals(30.0, fix.longitude);
        assertEquals(timestamp, fix.timestamp);
        assertEquals(123.0, fix.course);
        assertEquals(12.5, fix.speed);
        assertEquals(0, fix.synced);
        assertEquals(String.valueOf(998), fix.eventId);

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
	
	private void deleteAllGpsFixesFromDB()
	{
		ContentResolver cr = getContext().getContentResolver();
		cr.delete(SensorGps.CONTENT_URI, null, null);
		assertEquals(0, getAllGpsFixesFromDB().size());
		ExLog.i(getContext(), TAG, "deleteAllGpsFixesFromDB");
	}
	
	private ArrayList<GpsFix> getAllGpsFixesFromDB() 
	{
		ArrayList<GpsFix> result = new ArrayList<GpsFix>();
		String selectionClause = SensorGps.GPS_SYNCED + " = 0";
		// differs from trackingService projectionClause, taking event_id from sensor_gps.event_id,
		// because event does not exist.
		String projectionClauseStr = "sensor_gps.event_id as _eid,sensor_gps.gps_time,sensor_gps.gps_latitude,"
				+ "sensor_gps.gps_longitude,sensor_gps.gps_speed,sensor_gps.gps_bearing,sensor_gps.gps_synced,"
				+ "events.event_server,sensor_gps._id as _gid";
		ContentResolver cr = getContext().getContentResolver();
		Cursor cursor = cr.query(EventGpsFixesJoined.CONTENT_URI, projectionClauseStr.split(","), selectionClause, null, null);
		while (cursor.moveToNext()) {
			GpsFix gpsFix = new GpsFix();
			
			gpsFix.id = cursor.getInt(cursor.getColumnIndex("_gid"));
			gpsFix.timestamp = cursor.getLong(cursor.getColumnIndex(SensorGps.GPS_TIME));
			gpsFix.latitude  = cursor.getDouble(cursor.getColumnIndex(SensorGps.GPS_LATITUDE));
			gpsFix.longitude  = cursor.getDouble(cursor.getColumnIndex(SensorGps.GPS_LONGITUDE));
			gpsFix.speed  = cursor.getDouble(cursor.getColumnIndex(SensorGps.GPS_SPEED));
			gpsFix.course  = cursor.getDouble(cursor.getColumnIndex(SensorGps.GPS_BEARING));
			gpsFix.synced = cursor.getInt(cursor.getColumnIndex(SensorGps.GPS_SYNCED));
			gpsFix.host = cursor.getString(cursor.getColumnIndex(Event.EVENT_SERVER));
			gpsFix.eventId = cursor.getString(cursor.getColumnIndex("_eid"));
			
			result.add(gpsFix);
		}
		
		cursor.close();
		
		ExLog.i(getContext(), TAG, "getAllGpsFixesFromDB: " + result);
		return result;
	}
}
