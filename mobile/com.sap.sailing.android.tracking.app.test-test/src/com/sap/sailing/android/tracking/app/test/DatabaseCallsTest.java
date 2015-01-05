package com.sap.sailing.android.tracking.app.test;

import com.sap.sailing.android.tracking.app.utils.DatabaseHelper;

import android.app.Activity;

public class DatabaseCallsTest extends ActivityUnitTestCase<Activity> {

	public DatabaseCallsTest() {
		super(Activity.class);
	}
	
	public void testDeleteRegatta()
	{
		DatabaseTestHelper.createNewRegattaInDBAndReturnEventRowId(getActivity(), "TEST-EVENT",
				"123-456-789", "TEST-LEADERBOARD", "TEST-COMPETITOR");
		
		DatabaseTestHelper.createNewRegattaInDBAndReturnEventRowId(getActivity(), "TEST-EVENT 2",
				"234-567-890", "TEST-LEADERBOARD 2", "TEST-COMPETITOR 2");

		DatabaseHelper.getInstance().deleteRegattaFromDatabase(getActivity(), "TEST-EVENT",
				"TEST-COMPETITOR", "TEST-LEADERBOARD");
		
		assertEquals(1, DatabaseTestHelper.getNumberOfEventsFromDB(getActivity()));
		
		//TODO: Continue, ensure context is not null 
	}
	

}
