package com.sap.sailing.racecommittee.app.data.loaders;

import java.util.concurrent.Callable;

import android.content.Loader;
import android.os.Bundle;

import com.sap.sailing.racecommittee.app.data.clients.LoadClient;

public class ImmediateDataLoaderCallbacks<T> extends DataLoaderCallbacks<T> {
    public ImmediateDataLoaderCallbacks(LoadClient<T> clientCallback, final Callable<T> loadingFunc) {
        super(clientCallback, new LoaderCreator<T>() {
            @Override
            public Loader<DataLoaderResult<T>> create(int id, Bundle args) throws Exception {
                return new ImmediateDataLoader<T>(null, loadingFunc);
            }
        });
    }
}
