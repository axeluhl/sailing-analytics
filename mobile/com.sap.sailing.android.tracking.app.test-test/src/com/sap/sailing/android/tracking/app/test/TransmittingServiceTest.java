package com.sap.sailing.android.tracking.app.test;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import android.content.Intent;
import android.test.ServiceTestCase;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TrackingService;
import com.sap.sailing.android.tracking.app.services.TransmittingService;
import com.sap.sailing.android.tracking.app.utils.DatabaseHelper;
import com.sap.sailing.android.tracking.app.utils.VolleyHelper;
import com.sap.sailing.android.tracking.app.valueobjects.GpsFix;

public class TransmittingServiceTest extends ServiceTestCase<TransmittingService> {
	
	static final String TAG = TransmittingServiceTest.class.getName();
	final String eventId = "test123";
	
	private VolleyHelper volleyHelperSpy;
	private DatabaseHelper databaseHelperMock;
	
	public TransmittingServiceTest() {
		super(TransmittingService.class);
	}
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();        
        System.setProperty( "dexmaker.dexcache", getContext().getCacheDir().getPath() );
        DatabaseTestHelper.deleteAllEventsFromDB(getContext());
        DatabaseTestHelper.deleteAllGpsFixesFromDB(getContext());
        
        if (volleyHelperSpy == null)
        {
        	volleyHelperSpy = Mockito.spy(VolleyHelper.getInstance(getContext()));
        	VolleyHelper.injectInstance(volleyHelperSpy);
        }
        
        if (databaseHelperMock == null)
        {
        	databaseHelperMock = Mockito.mock(DatabaseHelper.class);
        	DatabaseHelper.injectInstance(databaseHelperMock);
        }
    }
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	private void startService()
	{
		Intent startIntent = new Intent();
        startIntent.setClass(getContext(), TrackingService.class);
        startIntent.setAction(getContext().getString(R.string.transmitting_service_start));
        startService(startIntent);
	}
	
	@SuppressWarnings("unchecked")
	public void testTransmittingServiceTransmitsGpsFix() throws InterruptedException, JSONException {
		long timestamp = (new Date()).getTime();
		
		ArgumentCaptor<JSONObject> jsonObjectCaptor = ArgumentCaptor.forClass(JSONObject.class);
		ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
	
		ArrayList<GpsFix> list = new ArrayList<GpsFix>();
		GpsFix fix = new GpsFix();
		fix.id = 99;
		fix.longitude = 12.0f;
		fix.latitude = 13.0f;
		fix.speed = 14.0f;
		fix.course = 101.5f;
		fix.eventId = "whatever";
		fix.timestamp = timestamp;
		fix.host = "http://127.0.0.1";
		list.add(fix);
		
		Mockito.when(databaseHelperMock.getUnsentFixes(Mockito.anyList(), Mockito.anyInt())).thenReturn(list);
		
		startService();
		
		Thread.sleep(4000); 

		Mockito.verify(volleyHelperSpy, Mockito.times(1)).enqueueRequest(
				urlCaptor.capture(), jsonObjectCaptor.capture(), 
				(Listener<JSONObject>)Mockito.any(), Mockito.any(ErrorListener.class));

		JSONObject json = jsonObjectCaptor.getValue();
		assertEquals(1, json.getJSONArray("fixes").length());
		
		JSONObject jsonFix = (JSONObject)json.getJSONArray("fixes").get(0);
		assertEquals(timestamp, jsonFix.getLong("timeMillis"));
		assertEquals(12, jsonFix.getLong("lonDeg"));
		assertEquals(13, jsonFix.getLong("latDeg"));
		assertEquals(14, jsonFix.getLong("speedMperS"));
		assertEquals(101.5, jsonFix.getDouble("bearingDeg"));
		
		shutdownService();
	}

}
