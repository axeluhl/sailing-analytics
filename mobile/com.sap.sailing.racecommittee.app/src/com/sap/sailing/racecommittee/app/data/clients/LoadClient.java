package com.sap.sailing.racecommittee.app.data.clients;


public interface LoadClient<T> {
	public void onFailed(Exception reason);
}
