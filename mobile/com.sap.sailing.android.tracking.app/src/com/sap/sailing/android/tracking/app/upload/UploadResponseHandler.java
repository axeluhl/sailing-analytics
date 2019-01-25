package com.sap.sailing.android.tracking.app.upload;

/**
 * Interface that
 *
 * @author Teresa Holfeld, holfeld@ubilabs.net
 */
public interface UploadResponseHandler {
    void onUploadTaskStarted();

    void onUploadCancelled();

    void onUploadTaskFinished(UploadResult uploadResult);
}
