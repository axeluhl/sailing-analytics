package com.sap.sailing.racecommittee.app.utils;

import java.net.URL;

import com.sap.sailing.domain.common.impl.DeviceConfigurationQRCodeUtils;
import com.sap.sailing.domain.common.impl.DeviceConfigurationQRCodeUtils.DeviceConfigurationDetails;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.autoupdate.AutoUpdater;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class QRHelper {

    private Context mContext;

    private QRHelper(Context context) {
        mContext = context;
    }

    public static QRHelper with(Context context) {
        return new QRHelper(context);
    }

    public boolean saveData(String content) {
        try {
            DeviceConfigurationDetails connectionConfiguration = DeviceConfigurationQRCodeUtils.splitQRContent(content);

            String identifier = connectionConfiguration.getDeviceIdentifier();
            URL apkUrl = UrlHelper.tryConvertToURL(connectionConfiguration.getApkUrl());

            if (apkUrl != null) {
                String serverUrl = UrlHelper.getServerUrl(apkUrl);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(mContext.getString(R.string.preference_identifier_key), identifier);
                editor.putString(mContext.getString(R.string.preference_server_url_key), serverUrl);
                editor.commit();

                new AutoUpdater(mContext).checkForUpdate(false);
                return true;
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.error_scanning_qr_malformed), Toast.LENGTH_LONG).show();
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return false;
    }
}
