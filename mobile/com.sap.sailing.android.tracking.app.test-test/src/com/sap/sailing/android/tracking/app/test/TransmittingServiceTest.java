package com.sap.sailing.android.tracking.app.test;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import android.content.Context;
import android.content.Intent;
import android.test.ServiceTestCase;

import com.sap.sailing.android.shared.data.http.HttpJsonPostRequest;
import com.sap.sailing.android.shared.data.http.HttpRequest;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TrackingService;
import com.sap.sailing.android.tracking.app.services.TransmittingService;
import com.sap.sailing.android.tracking.app.test.extensions.DatabaseHelperTestable;
import com.sap.sailing.android.tracking.app.test.extensions.NetworkHelperTestable;
import com.sap.sailing.android.tracking.app.utils.NetworkHelper.NetworkHelperFailureListener;
import com.sap.sailing.android.tracking.app.utils.NetworkHelper.NetworkHelperSuccessListener;
import com.sap.sailing.android.tracking.app.valueobjects.GpsFix;

public class TransmittingServiceTest extends ServiceTestCase<TransmittingService> {
	
	static final String TAG = TransmittingServiceTest.class.getName();
	final String eventId = "test123";
	
	private NetworkHelperTestable networkHelperSpy;
	private DatabaseHelperTestable databaseHelperMock;
	
	public TransmittingServiceTest() {
		super(TransmittingService.class);
	}
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();        
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().toString());
        DatabaseTestHelper.deleteAllEventsFromDB(getContext());
        DatabaseTestHelper.deleteAllGpsFixesFromDB(getContext());
        
        if (networkHelperSpy == null)
        {
        	networkHelperSpy = Mockito.spy(new NetworkHelperTestable(getContext()));
        	NetworkHelperTestable.injectInstance(networkHelperSpy);
        }
        
        if (databaseHelperMock == null)
        {
        	databaseHelperMock = Mockito.mock(DatabaseHelperTestable.class);
        	DatabaseHelperTestable.injectInstance(databaseHelperMock);
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
		
		ArgumentCaptor<HttpJsonPostRequest> requestCaptor = ArgumentCaptor.forClass(HttpJsonPostRequest.class);
		//ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
	
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
		
		Mockito.when(databaseHelperMock.getUnsentFixes(Mockito.any(Context.class), Mockito.anyList(), Mockito.anyInt())).thenReturn(list);
		
		startService();
		
		Thread.sleep(3500); 
		
		Mockito.verify(networkHelperSpy, Mockito.times(1)).executeHttpJsonRequestAsnchronously(requestCaptor.capture(), 
				(NetworkHelperSuccessListener)Mockito.any(), (NetworkHelperFailureListener)Mockito.any());


		JSONObject json = new JSONObject(requestCaptor.getValue().getRequestBody());
		assertEquals(1, json.getJSONArray("fixes").length());
		
		JSONObject jsonFix = (JSONObject)json.getJSONArray("fixes").get(0);
		assertEquals(timestamp, jsonFix.getLong("timestamp"));
		assertEquals(12, jsonFix.getLong("longitude"));
		assertEquals(13, jsonFix.getLong("latitude"));
		assertEquals(14, jsonFix.getLong("speed"));
		assertEquals(101.5, jsonFix.getDouble("course"));
		
		assertEquals("http://127.0.0.1/sailingserver/api/v1/gps_fixes", requestCaptor.getValue().getUrlAsString());
		shutdownService();
	}


}
