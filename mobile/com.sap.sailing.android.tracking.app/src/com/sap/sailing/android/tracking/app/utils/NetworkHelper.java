package com.sap.sailing.android.tracking.app.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

import com.sap.sailing.android.shared.data.http.HttpRequest;
import com.sap.sailing.android.shared.logging.ExLog;

import android.content.Context;
import android.os.AsyncTask;

public class NetworkHelper {

    private final static String TAG = NetworkHelper.class.getName();

    protected static NetworkHelper mInstance;
    protected static Context mContext;

    public static NetworkHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NetworkHelper();
            mContext = context.getApplicationContext();
        }

        return mInstance;
    }

    protected NetworkHelper() {
        super();
    }

    public void executeHttpJsonRequestAsnchronously(HttpRequest request, NetworkHelperSuccessListener successListener,
            NetworkHelperFailureListener failureListener) {

        NetworkRequestTask task = new NetworkRequestTask(successListener, failureListener);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, request);
    }

    private class NetworkRequestTask extends AsyncTask<HttpRequest, Void, Void> {
        private NetworkHelperSuccessListener successListener;
        private NetworkHelperFailureListener failureListener;

        private JSONObject response;
        private boolean isSuccess = false;
        private NetworkHelperError error;

        public NetworkRequestTask(NetworkHelperSuccessListener successListener,
                NetworkHelperFailureListener failureListener) {
            this.successListener = successListener;
            this.failureListener = failureListener;
        }

        @Override
        protected Void doInBackground(HttpRequest... params) {
            InputStream stream = null;
            HttpRequest request = params[0];
            try {
                stream = request.execute();
                String responseStr = readStream(stream);

                response = null;

                if (responseStr.length() > 0) {
                    response = new JSONObject(responseStr);
                }

                isSuccess = true;
            } catch (IOException e) {
                error = new NetworkHelperError(e.getMessage());
            } catch (JSONException e) {
                error = new NetworkHelperError(e.getMessage());
                ExLog.e(mContext, TAG, "Failed to parse JSON: " + e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (isSuccess) {
                successListener.performAction(response);
            } else {
                failureListener.performAction(error);
            }
        }

        private String readStream(InputStream inputStream) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            return new String(baos.toByteArray(), Charset.defaultCharset());
        }
    }

    /**
     * perfomAction called in case of network request success
     */
    public interface NetworkHelperSuccessListener {
        public void performAction(JSONObject response);
    }

    /**
     * perfomAction called in case of network request failure
     */
    public interface NetworkHelperFailureListener {
        public void performAction(NetworkHelperError e);
    }

    /**
     * Error class for NetworkHelper
     *
     */
    public class NetworkHelperError {
        String message;

        public NetworkHelperError(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
