package com.sap.sailing.racecommittee.app.data.loaders;


public class DataLoaderResult<T> {
    //private static final String TAG = DataLoaderResult.class.getName();

    private Exception exception;
    private T result;
    
    public DataLoaderResult(T result) {
        this.result = result;
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

}
