package com.sap.sailing.android.tracking.app.test;

import java.util.Date;

import org.mockito.Mockito;

import android.content.Intent;
import android.test.ServiceTestCase;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TrackingService;
import com.sap.sailing.android.tracking.app.services.TransmittingService;
import com.sap.sailing.android.tracking.app.utils.VolleyHelper;

public class TransmittingServiceTest extends ServiceTestCase<TransmittingService> {
	
	static final String TAG = TransmittingServiceTest.class.getName();
	final String eventId = "test123";
	
	private VolleyHelper volleyHelperSpy;
	
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
        	VolleyHelper volleyHelper = VolleyHelper.getInstance(getContext());
        	volleyHelperSpy = Mockito.spy(volleyHelper);
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
	
	public void testTest()
	{
		assertNotNull("TEST");
	}
	
	public void testTransmittingServiceTransmitsGpsFix() throws InterruptedException {
		long eventRowId = DatabaseTestHelper.createNewEventInDBAndReturnItsId(getContext(), "TEST", eventId);
		long timestamp = (new Date()).getTime();
		DatabaseTestHelper.createNewGpsFixInDatabase(getContext(), eventRowId, 12.0, 13.0, 14.0, timestamp);
		
		startService();
		
		Thread.sleep(5000);
		
		shutdownService();
	}
	


}
