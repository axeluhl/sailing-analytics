package com.sap.sailing.racecommittee.app.data.loaders;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.sap.sailing.racecommittee.app.data.handlers.DataHandler;
import com.sap.sailing.racecommittee.app.data.http.GetHttpRequest;
import com.sap.sailing.racecommittee.app.data.parsers.DataParser;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class DataLoader<T> extends AsyncTaskLoader<T> {
	private static final String TAG = DataLoader.class.getName();
	
	protected URI requestUri;
	protected DataParser<T> dataParser;
	protected DataHandler<T> dataHandler;
	protected volatile Exception lastException;

	public DataLoader(Context context, URI requestUri,
			DataParser<T> dataParser, DataHandler<T> dataHandler) {
		super(context);
		this.requestUri = requestUri;
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
		}
		return null;
	}

	private T loadDataInBackground() throws Exception {
		GetHttpRequest getRequest = new GetHttpRequest();
		InputStream inputStream = null;
		Reader inputReader = null;
		try {
			inputStream = getRequest.get(requestUri);
			inputReader = new InputStreamReader(inputStream);
			return dataParser.parse(inputReader);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
			if (inputReader != null) {
				inputReader.close();
			}
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
