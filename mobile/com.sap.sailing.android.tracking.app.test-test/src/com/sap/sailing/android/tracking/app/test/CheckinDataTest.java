package com.sap.sailing.android.tracking.app.test;

import com.sap.sailing.android.tracking.app.valueobjects.CheckinData;
import com.sap.sailing.android.tracking.app.valueobjects.CompetitorInfo;
import com.sap.sailing.android.tracking.app.valueobjects.EventInfo;
import com.sap.sailing.android.tracking.app.valueobjects.LeaderboardInfo;

import junit.framework.TestCase;

public class CheckinDataTest extends TestCase {

	private CheckinData checkinData;
	
	@Override
	protected void setUp() throws Exception {
		checkinData = new CheckinData();
		
		checkinData.leaderboardName = "lt";
		checkinData.eventId = "et";
		checkinData.eventName = "ent";
		checkinData.eventStartDateStr = "123";
		checkinData.eventEndDateStr = "456";
		checkinData.eventFirstImageUrl = "http://1.2.3.4/test.png";
		checkinData.eventServerUrl = "http://5.6.7.8";
		checkinData.checkinURL = "http://9.1.2.3/checkin";
		checkinData.competitorCountryCode = "DE";
		checkinData.competitorName = "cn";
		checkinData.competitorNationality = "GER";
		checkinData.competitorSailId = "SAIL 1";
		checkinData.competitorId = "1-2-3-4";
		checkinData.deviceUid = "0011223344";
		
		super.setUp();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testGetEvent()
	{
		EventInfo event = checkinData.getEvent();
		assertEquals("et", event.id);
		assertEquals("ent", event.name);
		assertEquals(123, event.startMillis);
		assertEquals(456, event.endMillis);
		assertEquals("http://1.2.3.4/test.png", event.imageUrl);
		assertEquals("http://5.6.7.8", event.server);
	}
	
	public void testGetCompetitor()
	{
		CompetitorInfo competitor = checkinData.getCompetitor();
		assertEquals("DE", competitor.countryCode);
		assertEquals("cn", competitor.name);
		assertEquals("GER", competitor.nationality);
		assertEquals("SAIL 1", competitor.sailId);
		assertEquals("1-2-3-4", competitor.id);
	}
	
	public void testGetLeaderboard()
	{
		LeaderboardInfo leaderboard = checkinData.getLeaderboard();
		assertEquals("lt", leaderboard.name);
	}
}
