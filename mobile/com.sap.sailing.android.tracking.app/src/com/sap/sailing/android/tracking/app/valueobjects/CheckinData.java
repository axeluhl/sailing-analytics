package com.sap.sailing.android.tracking.app.valueobjects;

public class CheckinData {
	// public String gcmId;
	public String leaderboardName;
	public String eventId;
	public String eventName;
	public String eventStartDateStr;
	public String eventEndDateStr;
	public String eventFirstImageUrl;
	public String eventServerUrl;
	public String checkinURL;
	public String competitorName;
	public String competitorId;
	public String competitorSailId;
	public String competitorNationality;
	public String competitorCountryCode;
	public String deviceUid;
	
	public EventInfo getEvent()
	{
		EventInfo event = new EventInfo();
		event.name = eventName;
		event.id = eventId;
		event.startMillis = Long.parseLong(eventStartDateStr);
		event.endMillis = Long.parseLong(eventEndDateStr);
		event.imageUrl = eventFirstImageUrl;
		event.server = eventServerUrl;
		return event;
	}
	
	public LeaderboardInfo getLeaderboard()
	{
		LeaderboardInfo leaderboard = new LeaderboardInfo();
		leaderboard.name = leaderboardName;
		return leaderboard;
	}
	
	public CompetitorInfo getCompetitor()
	{
		CompetitorInfo competitor = new CompetitorInfo();
		competitor.name = competitorName;
		competitor.id = competitorId;
		competitor.sailId = competitorSailId;
		competitor.nationality = competitorNationality;
		competitor.countryCode = competitorCountryCode;
		return competitor;
	}
}