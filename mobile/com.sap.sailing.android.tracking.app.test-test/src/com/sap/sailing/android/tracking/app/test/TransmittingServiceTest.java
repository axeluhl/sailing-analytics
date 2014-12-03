package com.sap.sailing.android.tracking.app.test;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ServiceTestCase;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Event;
import com.sap.sailing.android.tracking.app.services.TransmittingService;

public class TransmittingServiceTest extends ServiceTestCase<TransmittingService> {
	
	static final String TAG = TransmittingServiceTest.class.getName();
	
	public TransmittingServiceTest() {
		super(TransmittingService.class);
	}
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllEventsFromDB();
    }
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testTest()
	{
		assertNotNull("TEST");
	}
	
	private long createNewEventInDBAndReturnItsId(String eventName)
	{
		ContentResolver cr = getContext().getContentResolver();
		ContentValues cv = new ContentValues();
		cv.put(Event.EVENT_NAME, eventName);
		Uri uri = cr.insert(Event.CONTENT_URI, cv);
		return ContentUris.parseId(uri);
	}
	
	private void deleteAllEventsFromDB()
	{
		ContentResolver cr = getContext().getContentResolver();
		cr.delete(Event.CONTENT_URI, null, null);
		assertEquals(0, getNumberOfEventsFromDB());
		ExLog.i(getContext(), TAG, "deleteAllEventsFromDB");
	}
	
	private int getNumberOfEventsFromDB()
	{
		ContentResolver cr = getContext().getContentResolver();
		Cursor cursor = cr.query(Event.CONTENT_URI, null, null, null, null);
		int result = cursor.getCount();
		cursor.close();
		return result;
	}
}
