package com.sap.sailing.racecommittee.app.data;

import com.sap.sailing.racecommittee.app.AppConstants;

import android.content.Context;

public abstract class DataManager implements ReadonlyDataManager {

    public static ReadonlyDataManager create(Context context) {
        if (AppConstants.IS_DATA_OFFLINE) {
            return new OfflineDataManager(context, InMemoryDataStore.INSTANCE);
        }
        return new OnlineDataManager(context, InMemoryDataStore.INSTANCE);
    }

    protected Context context;
    protected DataStore dataStore;

    public DataManager(Context context, DataStore dataStore) {
        this.context = context;
        this.dataStore = dataStore;
    }

    public DataStore getDataStore() {
        return dataStore;
    }
    
    public Context getContext() {
        return context;
    }

}
