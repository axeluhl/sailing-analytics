package com.sap.sailing.racecommittee.app.data.loaders;

import java.util.List;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.sap.sailing.domain.common.Named;
import com.sap.sailing.racecommittee.app.data.handlers.DataHandler;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class NamedListLoader<T extends Named> extends AsyncTaskLoader<List<T>> {

	private static final String TAG = NamedListLoader.class.getName();
	
	private String requestUrl;
	private DataHandler<T> dataHandler;

	public NamedListLoader(Context context, String requestUrl, DataHandler<T> dataHandler) {
		super(context);
		this.requestUrl = requestUrl;
		this.dataHandler = dataHandler;
	}
	
	protected volatile Exception lastException;
	
	protected List<T> loadDataInBackground() throws Exception {
		return new GetHttpRequest<List<T>>().get(requestUrl, typeref);
	}
	
	@Override
	public List<T> loadInBackground() {
		lastException = null;
		try {
			return loadDataInBackground();
		} catch (Exception e) {
			lastException = e;
			ExLog.e(TAG, e.toString());
		}
		return null;

	}
	
	public void deliverResult (List<T> data){
		if (data == null) {
			dataHandler.onFailed(lastException);
		} else {
			dataHandler.onLoaded(data);
		}
	}
}
