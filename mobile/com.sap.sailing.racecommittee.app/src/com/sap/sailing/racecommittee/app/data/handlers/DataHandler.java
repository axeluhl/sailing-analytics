package com.sap.sailing.racecommittee.app.data.handlers;

import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;

public abstract class DataHandler<T> {
	protected OnlineDataManager manager;
	protected LoadClient<T> client;
	
	public DataHandler(OnlineDataManager manager, LoadClient<T> client) {
		this.manager = manager;
		this.client = client;
	}
	
	public void onLoaded(T data) {
		client.onLoadSucceded(data);
	}
	
	public void onFailed(Exception reason) {
		client.onLoadFailed(reason);
	}
}
