package com.sap.sailing.racecommittee.app.data.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.sap.sailing.racecommittee.app.data.handlers.DataHandler;
import com.sap.sailing.racecommittee.app.data.http.HttpGetRequest;
import com.sap.sailing.racecommittee.app.data.http.HttpRequest;
import com.sap.sailing.racecommittee.app.data.parsers.DataParser;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class OnlineDataLoader<T> extends AsyncTaskLoader<DataLoaderResult<T>> {
    private static final String TAG = OnlineDataLoader.class.getName();

    protected DataParser<T> dataParser;
    protected DataHandler<T> dataHandler;
    protected HttpRequest httpRequest;

    public OnlineDataLoader(Context context, URL requestUrl, DataParser<T> dataParser, DataHandler<T> dataHandler)
            throws MalformedURLException, IOException {
        this(context, new HttpGetRequest(requestUrl), dataParser, dataHandler);
    }

    public OnlineDataLoader(Context context, HttpRequest request, DataParser<T> dataParser, DataHandler<T> dataHandler) {
        super(context);
        this.httpRequest = request;
        this.dataParser = dataParser;
        this.dataHandler = dataHandler;
        
        ExLog.i(TAG, String.format("Loader created: %d", this.hashCode()));
    }
    
    @Override
    protected void onStartLoading() {
        if (dataHandler.hasCachedResults()) {
            ExLog.i(TAG, String.format("Using cached results... %d", this.hashCode()));
            deliverResult(new DataLoaderResult<T>(dataHandler.getCachedResults(), true));
        } else {
            ExLog.i(TAG, String.format("No cached results. Forcing load now %d", this.hashCode()));
            forceLoad();
        }
    }
    
    @Override
    protected void onForceLoad() {
        ExLog.i(TAG, String.format("Forcing load %d", this.hashCode()));
        super.onForceLoad();
    }
    
    @Override
    public void deliverResult(DataLoaderResult<T> result) {
        if (result.isSuccessful()) {
            dataHandler.onResult(result.getResult());
        }
        super.deliverResult(result);
    }

    @Override
    public DataLoaderResult<T> loadInBackground() {
        try {
            return new DataLoaderResult<T>(loadDataInBackground(), false);
        } catch (Exception e) {
            ExLog.ex(TAG, e);
            return new DataLoaderResult<T>(e);
        }
    }

    private T loadDataInBackground() throws Exception {
        Reader reader = null;
        try {
            InputStream inputStream = httpRequest.execute();
            reader = new InputStreamReader(inputStream);
            return dataParser.parse(reader);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

}
