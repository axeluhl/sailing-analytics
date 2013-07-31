package com.sap.sailing.racecommittee.app.data.loaders;

/**
 * <p>
 * Wrapper class for data loading results.
 * </p>
 * 
 * <p>
 * A {@link DataLoaderResult} may be successful or failed. It carries either the failure's {@link Exception} or the
 * actual result.
 * </p>
 * 
 * @param <T>
 *            result type.
 */
public class DataLoaderResult<T> {
    // private static final String TAG = DataLoaderResult.class.getName();

    private final Exception exception;
    private final boolean cached;
    private final T result;

    public DataLoaderResult(T result, boolean isCached) {
        this.result = result;
        this.cached = isCached;
        this.exception = null;
    }

    public DataLoaderResult(Exception exception) {
        this.exception = exception;
        this.cached = false;
        this.result = null;
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
