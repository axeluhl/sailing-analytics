package com.sap.sailing.android.tracking.app.utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

import com.sap.sailing.android.shared.data.http.HttpJsonPostRequest;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.ui.activities.AbstractBaseActivity;
import com.sap.sailing.android.shared.util.NetworkHelper;
import com.sap.sailing.android.shared.util.UniqueDeviceUuid;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;

public class CheckoutHelper {
    private static String TAG = CheckoutHelper.class.getName();

    public void checkoutCompetitor(AbstractBaseActivity activity, String leaderboardName, String eventServer, String competitorId,
        NetworkHelper.NetworkHelperSuccessListener successListener, NetworkHelper.NetworkHelperFailureListener failureListener) {
        AppPreferences prefs = new AppPreferences(activity);
        final String checkoutURLStr = eventServer
            + prefs.getServerCheckoutPath().replace("{leaderboard-name}", Uri.encode(leaderboardName));

        activity.showProgressDialog(R.string.please_wait, R.string.checking_out);

        JSONObject checkoutData = new JSONObject();
        try {
            checkoutData.put(DeviceMappingConstants.JSON_COMPETITOR_ID_AS_STRING, competitorId);
            checkoutData.put(DeviceMappingConstants.JSON_DEVICE_UUID, UniqueDeviceUuid.getUniqueId(activity));
            checkoutData.put(DeviceMappingConstants.JSON_TO_MILLIS, System.currentTimeMillis());
        } catch (JSONException e) {
            activity.dismissProgressDialog();
            activity.showErrorPopup(R.string.error, R.string.error_could_not_complete_operation_on_server_try_again);
            ExLog.e(activity, TAG, "Error populating checkout-data: " + e.getMessage());
            return;
        }

        try {
            HttpJsonPostRequest request = new HttpJsonPostRequest(activity, new URL(checkoutURLStr), checkoutData.toString());
            com.sap.sailing.android.shared.util.NetworkHelper.getInstance(activity).executeHttpJsonRequestAsync(request,
                successListener, failureListener);

        } catch (MalformedURLException e) {
            ExLog.w(activity, TAG, "Error, can't check out, MalformedURLException: " + e.getMessage());
        }
    }
}
