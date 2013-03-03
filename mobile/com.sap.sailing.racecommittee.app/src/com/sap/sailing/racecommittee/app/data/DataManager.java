package com.sap.sailing.racecommittee.app.data;

import com.sap.sailing.racecommittee.app.AppConstants;

import android.content.Context;

public abstract class DataManager implements ReadonlyDataManager {

	public static ReadonlyDataManager create(Context context) {
		if (AppConstants.IS_DATA_OFFLINE) {
			return new OfflineDataManager(InMemoryDataStore.INSTANCE);
		}
		return new OnlineDataManager(context, InMemoryDataStore.INSTANCE);
	}

	protected DataStore dataStore;
	
	public DataManager(DataStore dataStore) {
		this.dataStore = dataStore;
	}
	
	public DataStore getDataStore() {
		return dataStore;
	}
	
}
