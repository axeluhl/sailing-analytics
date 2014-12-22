package com.sap.sailing.android.tracking.app.ui.activities;

import java.net.URL;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.services.sending.MessageSendingService;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneUUIDIdentifierImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class LaunchActivity extends BaseActivity {
    private static int requestCodeQRCode = 42471;
    private static final String TAG = LaunchActivity.class.getName();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launch_activity);

        Button scanBarcode = (Button) findViewById(R.id.btnScanBarcode);
        scanBarcode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                requestQRCodeScan();
            }
        });
    }
    
    protected boolean requestQRCodeScan() {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, requestCodeQRCode);
            return true;
        } catch (Exception e) {    
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
            startActivity(marketIntent);
        }
        return false;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != requestCodeQRCode) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        
        if (resultCode == Activity.RESULT_OK) {
            String content = data.getStringExtra("SCAN_RESULT");
            ExLog.i(this, TAG, "Parsing URI: "+content);
            Uri uri = Uri.parse(content);
            String server = uri.getScheme() + "://";
            server += uri.getHost();
            if (uri.getPort() != -1) {
                server += ":" + uri.getPort();
            }
            prefs.setServerURL(server);
            String leaderboard = uri.getQueryParameter(DeviceMappingConstants.LEADERBOARD_NAME);
            String competitorIdAsString = uri.getQueryParameter(DeviceMappingConstants.COMPETITOR_ID_AS_STRING);
            String markIdAsString = uri.getQueryParameter(DeviceMappingConstants.MARK_ID_AS_STRING);
            DeviceIdentifier device = new SmartphoneUUIDIdentifierImpl(UUID.fromString(prefs.getDeviceIdentifier()));
            TimePoint from = MillisecondsTimePoint.now();
            String itemId = null;
            String itemType = null;
            
            JSONObject mappingStart = new JSONObject();
            try {
                mappingStart.put(DeviceMappingConstants.DEVICE_UUID, device.getStringRepresentation());
                mappingStart.put(DeviceMappingConstants.FROM_MILLIS, from.asMillis());
                if (competitorIdAsString != null) {
                    mappingStart.put(DeviceMappingConstants.COMPETITOR_ID_AS_STRING, competitorIdAsString);
                } else if (markIdAsString != null) {
                    mappingStart.put(DeviceMappingConstants.MARK_ID_AS_STRING, markIdAsString);
                }
            } catch (IllegalArgumentException e) {
                //deserializing the competitor/mark on the server would fail if the idAsString were used
                ExLog.e(this, TAG, "Found non-UUID as mapped item id - can only deal with UUIDs: " + itemId);
                Toast.makeText(this, "Did not get a UUID as item ID: " + itemId, Toast.LENGTH_LONG).show(); // FIXME i18n
            } catch (JSONException e) {
                ExLog.e(this, TAG, "Internal error trying to register device: " + e.getMessage());
                Toast.makeText(this, "Internal error trying to register device : " + e.getMessage(), Toast.LENGTH_LONG).show(); // FIXME i18n
            }
            String postCheckinUrl = getPostCheckinUrl(server, leaderboard);
            startService(MessageSendingService.createMessageIntent(this, postCheckinUrl, /* callbackPayload */ null,
                    UUID.randomUUID(), mappingStart.toString(), /* callbackClass */ null));
            ExLog.i(this, TAG, "Created mapping event");
            Toast.makeText(this, "Successfully created mapping between device and " + itemType, Toast.LENGTH_LONG).show();

            startActivity(new Intent(this, StartTrackingActivity.class));
        } else {
            Toast.makeText(this, "Error scanning QRCode (" + resultCode + ")", Toast.LENGTH_LONG).show();
        }
    }
    
    private String getPostCheckinUrl(String serverBaseUrl, String leaderboardName) {
        return serverBaseUrl+"/sailingserver/api/v1/leaderboards/"+leaderboardName+"/device_mappings/start";
    }

    //FIXME: currently duplicate code with racecommitte app (GeneralPreferenceFragment), refactor once maven build is available and thus update mechanism can be created for tracking app see bugs 2398 2399
    protected String getServerUrl(URL apkUrl) {
        String protocol = apkUrl.getProtocol();
        String host = apkUrl.getHost();
        String port = apkUrl.getPort() == -1 ? "" : ":" + apkUrl.getPort();
        return protocol + "://" + host + port;
    }
}