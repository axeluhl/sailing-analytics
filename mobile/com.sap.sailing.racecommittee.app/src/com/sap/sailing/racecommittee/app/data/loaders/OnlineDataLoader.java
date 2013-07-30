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
    }
    
    @Override
    protected void onStartLoading() {
        if (dataHandler.hasCachedResults()) {
            ExLog.i(TAG, "Using cached results...");
            deliverResult(new DataLoaderResult<T>(dataHandler.getCachedResults()));
        } else {
            ExLog.i(TAG, "No cached results. Forcing load now.");
            forceLoad();
        }
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
            Thread.sleep(4000);
            return new DataLoaderResult<T>(loadDataInBackground());
        } catch (Exception e) {
            ExLog.ex(TAG, e);
            return new DataLoaderResult<T>(e);
        }
    }
    
    public static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
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
