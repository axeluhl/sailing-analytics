package com.sap.sailing.racecommittee.app.ui.activities;

import java.util.Date;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.RaceApplication;

public class SystemInformationActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_information_view);
        setupVersionView();
        setupInstalledView();
        setupPersistenceView();
    }

    @Override
    protected void updateSendingServiceInformation() {
        TextView statusView = (TextView) findViewById(R.id.system_information_persistence_status);
        ListView waitingView = (ListView) findViewById(R.id.system_information_persistence_waiting);
        if (boundSendingService) {
            Date lastSuccessfulSend = sendingService.getLastSuccessfulSend();
            statusView.setText(String.format("Currently %d events waiting to be sent.\nLast successful sent was at: %s.", 
                    sendingService.getDelayedIntentsCount(), lastSuccessfulSend == null ? "never" : lastSuccessfulSend));
            
            waitingView.setAdapter(new ArrayAdapter<String>(this, 
                    android.R.layout.simple_list_item_1, 
                    sendingService.getDelayedIntensContent()));
        } else {
            statusView.setText(getString(R.string.generic_error));
        }
        
    }

    private void setupPersistenceView() {
        // status and list view is updated by updateSendingServiceInformation
        Button clearButton = (Button) findViewById(R.id.system_information_persistence_clear);
        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (boundSendingService) {
                    sendingService.clearDelayedIntents();
                } else {
                    Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupVersionView() {
        TextView identifierView = (TextView) findViewById(R.id.system_information_application_identifier);
        identifierView.setText(AppPreferences.on(getApplicationContext()).getDeviceIdentifier());
        
        TextView versionView = (TextView) findViewById(R.id.system_information_application_version);
        PackageInfo info = RaceApplication.getPackageInfo(getApplication());
        if (info == null) {
            versionView.setText(getString(R.string.generic_error));
        } else {
            versionView.setText(String.format("%s (Code %s)", info.versionName, info.versionCode));
        }
    }

    private void setupInstalledView() {
        TextView installView = (TextView) findViewById(R.id.system_information_application_install);
        PackageInfo info = RaceApplication.getPackageInfo(getApplication());
        if (info == null) {
            installView.setText(getString(R.string.generic_error));
        } else {
            Date installDate = new Date(info.lastUpdateTime);
            installView.setText(String.format("%s - %s", DateFormat.getLongDateFormat(this).format(installDate),
                    DateFormat.getTimeFormat(this).format(installDate)));
        }
    }

}
