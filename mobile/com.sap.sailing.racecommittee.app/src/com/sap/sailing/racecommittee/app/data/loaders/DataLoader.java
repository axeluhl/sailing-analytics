package com.sap.sailing.racecommittee.app.data.loaders;

import com.sap.sailing.racecommittee.app.data.handlers.DataHandler;

import android.content.AsyncTaskLoader;
import android.content.Context;

public class DataLoader<T> extends AsyncTaskLoader<T> {
	
	private DataHandler<T> dataHandler;

	public DataLoader(Context context, DataHandler<T> dataHandler) {
		super(context);
		this.dataHandler = dataHandler;
	}
	
	@Override
	public T loadInBackground() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void deliverResult(T data) {
		if (data == null) {
			dataHandler.onFailed(null);
		} else {
			dataHandler.onLoaded(data);
		}
	}

}
