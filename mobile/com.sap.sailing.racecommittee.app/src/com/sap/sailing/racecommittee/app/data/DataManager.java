package com.sap.sailing.racecommittee.app.data;

import android.content.Context;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.racecommittee.app.AppPreferences;

/**
 * Base class for all data managers. Use {@link DataManager#create(Context)} for creating your {@link DataManager}.
 */
public abstract class DataManager implements ReadonlyDataManager {

    public static ReadonlyDataManager create(Context context) {
        DataStore dataStore = InMemoryDataStore.INSTANCE;
        if (AppPreferences.on(context).isOfflineMode()) {
            return new OfflineDataManager(context, dataStore, dataStore.getDomainFactory());
        }
        return new OnlineDataManager(context, dataStore, dataStore.getDomainFactory());
    }

    protected final AppPreferences preferences;
    protected final Context context;
    protected final DataStore dataStore;
    protected final SharedDomainFactory domainFactory;

    protected DataManager(Context context, DataStore dataStore, SharedDomainFactory domainFactory) {
        this.context = context;
        this.dataStore = dataStore;
        this.domainFactory = domainFactory;
        this.preferences = AppPreferences.on(context);
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public Context getContext() {
        return context;
    }

}
