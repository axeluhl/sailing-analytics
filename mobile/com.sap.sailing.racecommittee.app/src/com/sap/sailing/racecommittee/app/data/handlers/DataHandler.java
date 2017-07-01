package com.sap.sailing.racecommittee.app.data.handlers;

import com.sap.sailing.racecommittee.app.data.OnlineDataManager;

public abstract class DataHandler<T> {
    protected OnlineDataManager manager;

    public DataHandler(OnlineDataManager manager) {
        this.manager = manager;
    }

    public abstract void onResult(T data, boolean isCached);

    public boolean hasCachedResults() {
        return false;
    }

    public T getCachedResults() {
        return null;
    }

    public void clearCache() {
        // no op
    }
}
