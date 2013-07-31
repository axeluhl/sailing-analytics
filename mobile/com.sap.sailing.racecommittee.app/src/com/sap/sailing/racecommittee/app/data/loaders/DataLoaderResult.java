package com.sap.sailing.racecommittee.app.data.loaders;


public class DataLoaderResult<T> {
    //private static final String TAG = DataLoaderResult.class.getName();

    private Exception exception;
    private boolean cached;
    private T result;
    
    public DataLoaderResult(T result, boolean isCached) {
        this.result = result;
        this.cached = isCached;
    }
    
    public DataLoaderResult(Exception exception) {
        this.exception = exception;
    }
    
    public boolean isSuccessful() {
        return exception == null;
    }
 
    public Exception getException() {
        return exception;
    }
 
    public T getResult() {
        return result;
    }

    public boolean isResultCached() {
        if (!isSuccessful()) {
            throw new IllegalStateException("Failures cannot be cached!");
        }
        return cached;
    }

}
