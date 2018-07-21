package com.sap.sailing.racecommittee.app.services.polling;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;

import com.sap.sailing.android.shared.data.http.HttpJsonPostRequest;
import com.sap.sailing.android.shared.data.http.HttpRequest;
import com.sap.sailing.racecommittee.app.domain.racelog.impl.RaceLogEventsCallback;
import com.sap.sse.common.Util;

public class RaceLogPollingTask extends AsyncTask<Util.Pair<String, URL>, /* Progress */ Void, Void> {

    public interface PollingResultListener {
        void onPollingFinished();
    }
    
    private final PollingResultListener mListener;
    private final Context mContext;

    public RaceLogPollingTask(PollingResultListener listener, Context context) {
        mListener = listener;
        mContext = context.getApplicationContext();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Void doInBackground(Util.Pair<String, URL>... queries) {
        for (Util.Pair<String, URL> query : queries) {
            if (isCancelled()) {
                return null;
            }
            
            HttpRequest request = new HttpJsonPostRequest(mContext, query.getB());
            InputStream responseStream;
            try {
                responseStream = request.execute();
                String raceId = query.getA();
                new RaceLogEventsCallback().processResponse(mContext, responseStream, raceId);
            } catch (IOException e) {
                // don't need to close responseStream as it still must
                // be null because the only call that may throw an
                // IOException is the execute() call that would have
                // initialized responseStream
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (mListener != null) {
            mListener.onPollingFinished();
        }
    }
}
