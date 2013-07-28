package com.sap.sailing.racecommittee.app.data.loaders;

import java.util.concurrent.Callable;

import android.content.Context;
import android.content.Loader;

public class ImmediateDataLoader<T> extends Loader<DataLoaderResult<T>> {
    //private static final String TAG = StoreDataLoader.class.getName();
    
    private Callable<T> resultCallable;

    public ImmediateDataLoader(Context context, Callable<T> resultCallable) {
        super(context);
        this.resultCallable = resultCallable;
    }
    
    @Override
    protected void onStartLoading() {
        publishResult();
    }
    
    @Override
    protected void onForceLoad() {
        publishResult();
    }

    private void publishResult() {
        try {
            deliverResult(new DataLoaderResult<T>(resultCallable.call()));
        } catch (Exception e) {
            deliverResult(new DataLoaderResult<T>(e));
        }
    }
}
