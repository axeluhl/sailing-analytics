package com.sap.sailing.racecommittee.app.data.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.sap.sailing.racecommittee.app.data.handlers.DataHandler;
import com.sap.sailing.racecommittee.app.data.http.HttpGetRequest;
import com.sap.sailing.racecommittee.app.data.http.HttpRequest;
import com.sap.sailing.racecommittee.app.data.parsers.DataParser;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class DataLoader<T> extends AsyncTaskLoader<T> {
    private static final String TAG = DataLoader.class.getName();

    protected DataParser<T> dataParser;
    protected DataHandler<T> dataHandler;
    protected HttpRequest httpRequest;
    protected volatile Exception lastException;

    public DataLoader(Context context, URI httpGetUri, DataParser<T> dataParser, DataHandler<T> dataHandler)
            throws MalformedURLException, IOException {
        this(context, new HttpGetRequest(httpGetUri), dataParser, dataHandler);
    }

    public DataLoader(Context context, HttpRequest request, DataParser<T> dataParser, DataHandler<T> dataHandler) {
        super(context);
        this.httpRequest = request;
        this.dataParser = dataParser;
        this.dataHandler = dataHandler;
    }

    @Override
    public T loadInBackground() {
        lastException = null;
        try {
            return loadDataInBackground();
        } catch (Exception e) {
            lastException = e;
            ExLog.e(TAG, String.format("Exception while loading data:\n%s", e.toString()));
            StringWriter stringWriter = new StringWriter();
            PrintWriter stream = new PrintWriter(stringWriter);
            e.printStackTrace(stream);
            ExLog.e(TAG, stringWriter.toString());
        }
        return null;
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
            httpRequest.disconnect();
        }
    }

    @Override
    public void deliverResult(T data) {
        if (data == null) {
            dataHandler.onFailed(lastException);
        } else {
            dataHandler.onLoaded(data);
        }
    }

}
