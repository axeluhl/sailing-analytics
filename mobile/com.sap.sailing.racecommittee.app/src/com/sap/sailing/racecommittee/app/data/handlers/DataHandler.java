package com.sap.sailing.racecommittee.app.data.handlers;

import android.widget.Toast;

import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;

public abstract class DataHandler<T> {
	protected DataManager manager;
	protected LoadClient<T> client;
	
	public DataHandler(DataManager manager, LoadClient<T> client) {
		this.manager = manager;
		this.client = client;
	}
	
	public void onLoaded(T data) {
		Toast.makeText(manager.getContext(), 
				String.format("Loaded from %s.", 
						AppConstants.getURL(manager.getContext())), Toast.LENGTH_SHORT).show();
	}
	
	public void onFailed(Exception reason) {
		client.onFailed(reason);
	}
}
