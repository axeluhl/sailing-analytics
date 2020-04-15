package com.sap.sailing.android.tracking.app.upload;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.R;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Uploads an image to the server.
 *
 * @author Teresa Holfeld, holfeld@ubilabs.net
 */
public class UploadTeamImageTask extends AsyncTask<String, Void, UploadResult> {

    private static final String TAG = UploadTeamImageTask.class.getName();
    private final static int ERROR_IO = 600;

    private Context context;
    private File imageFile;
    private String uploadUrl;
    private UploadResponseHandler uploadResponseHandler;

    public UploadTeamImageTask(Context context, File imageFile, UploadResponseHandler uploadResponseHandler) {
        this.context = context;
        this.imageFile = imageFile;
        this.uploadResponseHandler = uploadResponseHandler;
    }

    @Override
    protected void onPreExecute() {
        uploadResponseHandler.onUploadTaskStarted();
    }

    protected UploadResult doInBackground(String... urls) {
        uploadUrl = urls[0];
        DataOutputStream outputStream = null;
        FileInputStream imageInputStream = null;
        BufferedReader reader = null;
        UploadResult uploadResult = new UploadResult();
        try {
            if (imageFile != null) {
                URL url = new URL(uploadUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setUseCaches(false);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "image/jpeg");
                outputStream = new DataOutputStream(urlConnection.getOutputStream());

                int nRead;
                byte[] data = new byte[2048];
                imageInputStream = new FileInputStream(imageFile);
                while ((nRead = imageInputStream.read(data, 0, data.length)) != -1) {
                    outputStream.write(data, 0, nRead);
                }
                imageInputStream.close();
                outputStream.flush();
                outputStream.close();
                uploadResult.resultCode = urlConnection.getResponseCode();
                uploadResult.resultMessage = urlConnection.getResponseMessage();
                Log.d(TAG, "Image upload response: " + uploadResult.resultCode);
                if (uploadResult.resultCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader((urlConnection.getInputStream())));
                    StringBuilder builder = new StringBuilder();
                    String output;
                    while ((output = reader.readLine()) != null) {
                        builder.append(output);
                    }
                    Log.d(TAG, "Response body: " + builder.toString());
                }
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            uploadResult.resultCode = ERROR_IO;
            uploadResult.resultMessage = context.getString(R.string.upload_io_error) + e.getLocalizedMessage();
            ExLog.e(context, TAG, uploadResult.resultCode + ": " + uploadResult.resultMessage);
        } finally {
            safeClose(outputStream);
            safeClose(imageInputStream);
            safeClose(reader);
        }
        return uploadResult;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        uploadResponseHandler.onUploadCancelled();
    }

    @Override
    protected void onPostExecute(UploadResult uploadResult) {
        uploadResponseHandler.onUploadTaskFinished(uploadResult);
        uploadResponseHandler = null;
    }

    private void safeClose(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                ExLog.ex(context, TAG, e);
            }
        }
    }
}
