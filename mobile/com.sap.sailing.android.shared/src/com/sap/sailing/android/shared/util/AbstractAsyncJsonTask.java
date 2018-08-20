package com.sap.sailing.android.shared.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sailing.android.shared.data.http.HttpRequest;

import android.content.Context;
import android.os.AsyncTask;

public abstract class AbstractAsyncJsonTask<Params, Progress, Result, ListenerType extends AbstractAsyncTaskListener<Result>> extends AsyncTask<Params, Progress, Result> {
    private URL mUrl;
    private final Context mContext;
    private final WeakReference<ListenerType> mListener;
    private Exception mException;

    /**
     * @param listener
     *            a listener to be called back when task execution has completed; the listener will only be
     *            {@link WeakReference referenced weakly} here, so if the caller lets go of all strong references to it
     *            then it becomes eligible for garbage collection, and no call back will take place anymore.
     */
    public AbstractAsyncJsonTask(Context context, ListenerType listener, URL url) {
        mContext = context.getApplicationContext();
        mListener = new WeakReference<>(listener);
        mUrl = url;
    }

    protected URL getUrl() {
        return mUrl;
    }

    protected Context getContext() {
        return mContext;
    }

    protected WeakReference<ListenerType> getListener() {
        return mListener;
    }

    protected Exception getException() {
        return mException;
    }

    @Override
    protected Result doInBackground(@SuppressWarnings("unchecked") Params... params) {
        Result result = null;
        if (mUrl != null) {
            try {
                HttpRequest request;
                if (params != null && params.length > 0) {
                    request = createRequest(params[0]);
                } else {
                    request = createRequest(null);
                }
                InputStream responseStream = request.execute();

                JSONParser parser = new JSONParser();
                JSONObject jsonResult = (JSONObject) parser.parse(new InputStreamReader(responseStream));
                result = getResult(jsonResult);
            } catch (Exception e) {
                mException = e;
            }
        } else {
            mException = new IllegalArgumentException();
        }
        return result;
    }

    abstract protected Result getResult(JSONObject jsonResult);

    abstract protected HttpRequest createRequest(Params params);

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        ListenerType listener = mListener.get();
        if (listener != null) {
            if (mException != null) {
                listener.onException(mException);
            } else {
                listener.onResultReceived(result);
            }
        }
    }
}
