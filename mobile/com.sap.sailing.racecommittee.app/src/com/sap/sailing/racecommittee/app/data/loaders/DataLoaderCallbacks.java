package com.sap.sailing.racecommittee.app.data.loaders;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;

import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.logging.ExLog;


public class DataLoaderCallbacks<T> implements LoaderCallbacks<DataLoaderResult<T>> {
    private static String TAG = DataLoaderCallbacks.class.getName();
    
    public interface LoaderCreator<T> {
        Loader<DataLoaderResult<T>> create(int id, Bundle args) throws Exception;
    }
    
    private LoadClient<T> clientCallback;
    private LoaderCreator<T> loaderCreator;

    public DataLoaderCallbacks(LoadClient<T> clientCallback, LoaderCreator<T> loaderCreator) {
        this.clientCallback = clientCallback;
        this.loaderCreator = loaderCreator;
    }

    @Override
    public Loader<DataLoaderResult<T>> onCreateLoader(int id, Bundle args) {
        try {
            return loaderCreator.create(id, args);
        } catch (Exception e) {
            ExLog.e(TAG, String.format("Exception while trying to create loader:\n%s", e.toString()));
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<DataLoaderResult<T>> loader, DataLoaderResult<T> result) {
        if (result.isSuccessful()) {
            clientCallback.onLoadSucceded(result.getResult());
        } else {
            clientCallback.onLoadFailed(result.getException());
        }
    }

    @Override
    public void onLoaderReset(Loader<DataLoaderResult<T>> loader) {
        // currently we ignore loader resets...
    }


}
