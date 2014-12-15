package com.sap.sailing.android.tracking.app.valueobjects;

public class EventInfo {

	public String id;
	public String name;
	public String leaderboardName; // when using join-query
	public String imageUrl;
	public long startMillis;
	public long endMillis;
	public int rowId;

	@Override
	public String toString() {
		return "eventName: " + name + ", leaderboardName: "
				+ leaderboardName + ", eventImageUrl: " + imageUrl
				+ ", eventStartMillis: " + startMillis
				+ ", eventEndMillis: " + endMillis + ", eventRowId: "
				+ rowId;
	}
}
