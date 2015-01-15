package com.sap.sailing.android.tracking.app.test;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.utils.DatabaseHelper;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.view.ContextThemeWrapper;

public class DatabaseCallsTest extends ActivityUnitTestCase<Activity> {

	private Intent mStartIntent;

	
	public DatabaseCallsTest() {
		super(Activity.class);
	}
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();        
		mStartIntent = new Intent(Intent.ACTION_MAIN);
		startActivity(mStartIntent, null, null);
		DatabaseTestHelper.deleteAllEventsFromDB(getActivity());
    }
	
	public void testDeleteRegatta()
	{
		DatabaseTestHelper.createNewRegattaInDBAndReturnEventRowId(getActivity(), "TEST-EVENT",
				"123-456-789", "TEST-LEADERBOARD", "TEST-COMPETITOR", "TEST-DIGEST1");
		
		DatabaseTestHelper.createNewRegattaInDBAndReturnEventRowId(getActivity(), "TEST-EVENT 2",
				"234-567-890", "TEST-LEADERBOARD 2", "TEST-COMPETITOR 2", "TEST-DIGEST2");

		DatabaseHelper.getInstance().deleteRegattaFromDatabase(getActivity(), "TEST-DIGEST1");
		
		assertEquals(1, DatabaseTestHelper.getNumberOfEventsFromDB(getActivity()));
		
		//TODO: Continue, ensure context is not null 
	}
	

}
