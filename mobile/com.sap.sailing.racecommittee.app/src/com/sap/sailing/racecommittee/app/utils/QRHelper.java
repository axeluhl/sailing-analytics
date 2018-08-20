package com.sap.sailing.racecommittee.app.utils;

import java.io.UnsupportedEncodingException;
import java.net.URL;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.common.impl.DeviceConfigurationQRCodeUtils;
import com.sap.sailing.domain.common.impl.DeviceConfigurationQRCodeUtils.DeviceConfigurationDetails;
import com.sap.sailing.domain.common.impl.DeviceConfigurationQRCodeUtils.URLDecoder;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.autoupdate.AutoUpdater;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class QRHelper {
    private static final String TAG = QRHelper.class.getName();
    private Context mContext;

    private QRHelper(Context context) {
        mContext = context;
    }

    public static QRHelper with(Context context) {
        return new QRHelper(context);
    }

    public boolean saveData(String content) {
        try {
            DeviceConfigurationDetails connectionConfiguration = DeviceConfigurationQRCodeUtils.splitQRContent(content, new URLDecoder() {
                @Override
                public String decode(String encodedURL) {
                    try {
                        return java.net.URLDecoder.decode(encodedURL, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        ExLog.w(mContext, TAG, "Couldn't resolve encoding UTF-8");
                        return encodedURL;
                    }
                }
            });

            String identifier = connectionConfiguration.getDeviceIdentifier();
            URL apkUrl = UrlHelper.tryConvertToURL(connectionConfiguration.getApkUrl());
            String accessToken = connectionConfiguration.getAccessToken();

            if (apkUrl != null) {
                String serverUrl = UrlHelper.getServerUrl(apkUrl);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(mContext.getString(R.string.preference_identifier_key), identifier);
                editor.putString(mContext.getString(R.string.preference_server_url_key), serverUrl);
                editor.putString(mContext.getString(R.string.preference_access_token_key), accessToken);
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
