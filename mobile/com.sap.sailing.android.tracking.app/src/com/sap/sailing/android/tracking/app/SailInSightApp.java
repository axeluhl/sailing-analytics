package com.sap.sailing.android.tracking.app;

import com.sap.sailing.android.shared.application.LoggableApplication;

import java.io.File;
import java.util.HashMap;

public class SailInSightApp extends LoggableApplication {

    private HashMap<String, File> mUploadImage = new HashMap<>();

    public boolean isLastUploadFailed(String id) {
        return mUploadImage.containsKey(id);
    }

    public void setUploadImage(String id, File image) {
        mUploadImage.put(id, image);
    }

    public File getUploadImage(String id) {
        if (mUploadImage.containsKey(id)) {
            return mUploadImage.get(id);
        } else {
            return null;
        }
    }

    public void removeUploadImage(String id) {
        if (mUploadImage.containsKey(id)) {
            mUploadImage.remove(id);
        }
    }
}
