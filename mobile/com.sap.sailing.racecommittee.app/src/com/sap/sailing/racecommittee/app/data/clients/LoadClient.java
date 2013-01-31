package com.sap.sailing.racecommittee.app.data.clients;

public interface LoadClient<T> {
	public void onLoadFailed(Exception reason);
	public void onLoadSucceded(T data);
}
