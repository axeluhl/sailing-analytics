package com.sap.sailing.racecommittee.app.services;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.sap.sailing.android.shared.data.LoginData;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.AuthCheckTask;
import com.sap.sailing.android.shared.util.BroadcastManager;
import com.sap.sailing.android.shared.util.LoginTask;
import com.sap.sailing.domain.common.impl.DeviceConfigurationQRCodeUtils;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.ui.fragments.LoginBackdrop;
import com.sap.sailing.racecommittee.app.utils.QRHelper;

import java.net.MalformedURLException;

public class BoardingService {
    private static final String TAG = BoardingService.class.getName();
    private static BoardingService INSTANCE;


    public static BoardingService get() {
        synchronized (BoardingService.class) {
            if (INSTANCE == null) {
                INSTANCE = new BoardingService();
            }
        }
        return INSTANCE;
    }

    public boolean saveBackendUrl(@NonNull final Context context, @NonNull final String url) {
        AppPreferences pref = AppPreferences.on(context);
        final String deviceId = pref.getDeviceConfigurationName(null);
        if (QRHelper.with(context)
                .saveData(url + "#"
                        + DeviceConfigurationQRCodeUtils.deviceIdentifierKey + "="
                        + deviceId)) {
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(new Intent(AppConstants.INTENT_ACTION_CHECK_LOGIN));
            return true;
        } else {
            return false;
        }
    }

    public boolean checkLogin(@NonNull final Context context, AuthCheckTask.AuthCheckTaskListener authCheckTaskListener) {

        AppPreferences pref = AppPreferences.on(context);
        if (TextUtils.isEmpty(pref.getServerBaseURL())) {
            return false;
        } else {
            try {
                AuthCheckTask task = new AuthCheckTask(context, pref.getServerBaseURL(), authCheckTaskListener);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            } catch (MalformedURLException e) {
                ExLog.e(context, TAG,
                        "Error: Failed to perform check-in due to a MalformedURLException: " + e.getMessage());
                return false;
            }
        }
    }

    public void login(@NonNull final Context context, @NonNull final String email, @NonNull final String password, LoginTask.LoginTaskListener loginTaskListener) {
        LoginTask task;
        try {
            task = new LoginTask(context, AppPreferences.on(context).getServerBaseURL(),
                    loginTaskListener);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    new LoginData(email, password));
        } catch (Exception e) {
            ExLog.e(context, TAG,
                    "Error: Failed to perform checkin due to a MalformedURLException: " + e.getMessage());
        }
    }
}
