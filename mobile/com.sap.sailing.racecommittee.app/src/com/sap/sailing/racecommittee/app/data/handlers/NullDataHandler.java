package com.sap.sailing.racecommittee.app.data.handlers;

import com.sap.sailing.racecommittee.app.data.DataManager;

/**
 * Use this {@link DataHandler} whenever the {@link DataManager} should not know anything about the result.
 */
public class NullDataHandler<T> extends DataHandler<T> {

    public NullDataHandler() {
        super(null);
    }

    @Override
    public void onResult(T data, boolean isCached) {
        // no operation
    }

}
