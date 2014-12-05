package com.sap.sailing.android.tracking.app.ui.activities;

import java.net.URL;
import java.util.UUID;

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
import com.sap.sailing.android.tracking.app.utils.AppPreferences;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventFactory;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.common.racelog.RaceLogServletConstants;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneUUIDIdentifierImpl;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogEventSerializer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class LaunchActivity extends BaseActivity {
    private final JsonSerializer<RaceLogEvent> eventSerializer =
            RaceLogEventSerializer.create(new CompetitorJsonSerializer());;
    
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
            String leaderboard = uri.getQueryParameter(RaceLogServletConstants.PARAMS_LEADERBOARD_NAME);
            String raceColumn = uri.getQueryParameter(RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME);
            String fleet = uri.getQueryParameter(RaceLogServletConstants.PARAMS_RACE_FLEET_NAME);
            String competitorIdAsString = uri.getQueryParameter(DeviceMappingConstants.COMPETITOR_ID_AS_STRING);
            String markIdAsString = uri.getQueryParameter(DeviceMappingConstants.MARK_ID_AS_STRING);
            
            @SuppressWarnings("deprecation")
            Long fromMillis = Long.parseLong(uri.getQueryParameter(DeviceMappingConstants.FROM_MILLIS));
            @SuppressWarnings("deprecation")
            Long toMillis = Long.parseLong(uri.getQueryParameter(DeviceMappingConstants.TO_MILLIS));
            
            RaceLogEvent event = null;

            DeviceIdentifier device = new SmartphoneUUIDIdentifierImpl(
                    UUID.fromString(prefs.getDeviceIdentifier()));
            TimePoint from = new MillisecondsTimePoint(fromMillis);
            TimePoint to = new MillisecondsTimePoint(toMillis);
            String itemId = null;
            String itemType = null;
            try {
                if (competitorIdAsString != null) {
                    itemId = competitorIdAsString;
                    itemType = "Competitor";
                    UUID competitorId = UUID.fromString(competitorIdAsString);
                    Competitor competitor = new CompetitorImpl(competitorId, null, null, null, null);
                    event = RaceLogEventFactory.INSTANCE.createDeviceCompetitorMappingEvent(
                            MillisecondsTimePoint.now(), AppPreferences.raceLogEventAuthor, device,
                            competitor, 0, from, to);
                } else if (markIdAsString != null) {
                    itemId = markIdAsString;
                    itemType = "Mark";
                    UUID markId = UUID.fromString(markIdAsString);
                    Mark mark = new MarkImpl(markId, null);
                    event = RaceLogEventFactory.INSTANCE.createDeviceMarkMappingEvent(
                            MillisecondsTimePoint.now(), AppPreferences.raceLogEventAuthor, device,
                            mark, 0, from, to);
                }
            } catch (IllegalArgumentException e) {
                //deserializing the competitor/mark on the server would fail if the idAsString were used
                ExLog.e(this, TAG, "Found non-UUID as mapped item id - can only deal with UUIDs: " + itemId);
                Toast.makeText(this, "Did not get a UUID as item ID: " + itemId, Toast.LENGTH_LONG).show();
            }

            if (event == null) {
                ExLog.e(this, TAG, "Could not find the necessary information in the QRCode URL: " + content);
                Toast.makeText(this, "Could not find the necessary information in " + content,
                        Toast.LENGTH_LONG).show();
            } else {
                String url = MessageSendingService.getRaceLogEventSendAndReceiveUrl(
                        this, leaderboard, raceColumn, fleet);
                String eventJson = eventSerializer.serialize(event).toString();
                startService(MessageSendingService.createMessageIntent(this, url,
                        null, event.getId(), eventJson, null));
                ExLog.i(this, TAG, "Created mapping event");
                Toast.makeText(this, "Successfully created mapping between device and " + itemType,
                        Toast.LENGTH_LONG).show();
                
                startActivity(new Intent(this, StartTrackingActivity.class));
            }
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