package com.sap.sailing.android.tracking.app.ui.activities;

import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;

public class LaunchActivity extends BaseActivity {
    
    private static int requestCodeQRCode = 42471;
    private static final String TAG = LaunchActivity.class.getName();
    private final AppPreferences prefs = new AppPreferences(this);
    
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
            String host = uri.getHost();
            prefs.setServerURL(host);
            String leaderboard = uri.getQueryParameter("leaderboard");
            String raceColumn = uri.getQueryParameter("raceColumn");
            String fleet = uri.getQueryParameter("fleet");
            String competitor = uri.getQueryParameter("competitor");
            //TODO: write RacelogDeviceCompetitorMappingEvent
        } else {
            Toast.makeText(this, "Error scanning QRCode (" + resultCode + ")", Toast.LENGTH_LONG).show();
        }
    }
    
    //FIXME: currently duplicate code with racecommitte app (GeneralPreferenceFragment), refactor once maven build is available and thus update mechanism can be created for tracking app see bugs 2398 2399
    protected String getServerUrl(URL apkUrl) {
        String protocol = apkUrl.getProtocol();
        String host = apkUrl.getHost();
        String port = apkUrl.getPort() == -1 ? "" : ":" + apkUrl.getPort();
        return protocol + "://" + host + port;
    }
}