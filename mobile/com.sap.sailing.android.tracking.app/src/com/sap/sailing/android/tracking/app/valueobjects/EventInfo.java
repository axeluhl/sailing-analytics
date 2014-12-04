package com.sap.sailing.android.tracking.app.valueobjects;

public class EventInfo {

	public String eventName;
	public String leaderboardName;
	
	@Override
	public String toString() {
		return "eventName: " + eventName + ", leaderboardName: " + leaderboardName;
	}
}
