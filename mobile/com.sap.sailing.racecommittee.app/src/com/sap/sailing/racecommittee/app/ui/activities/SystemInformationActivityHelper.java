package com.sap.sailing.racecommittee.app.ui.activities;

import java.util.Date;

import android.content.pm.PackageInfo;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.sailing.android.shared.R;
import com.sap.sailing.android.shared.util.AppUtils;

public class SystemInformationActivityHelper {
    private final SendingServiceAwareActivity activity;

    public SystemInformationActivityHelper(SendingServiceAwareActivity activity, String deviceIdentifier) {
        this.activity = activity;
        activity.setContentView(R.layout.system_information_view);
        setupVersionView(deviceIdentifier);
        setupInstalledView();
        setupPersistenceView();
    }

    /**
     * Override the SendingServiceInformation with this content, and then call parent.
     */
    public void updateSendingServiceInformation() {
        TextView statusView = (TextView) activity.findViewById(R.id.system_information_persistence_status);
        ListView waitingView = (ListView) activity.findViewById(R.id.system_information_persistence_waiting);
        if (activity.boundSendingService) {
            Date lastSuccessfulSend = activity.sendingService.getLastSuccessfulSend();
            statusView.setText(String.format("Currently %d events waiting to be sent.\nLast successful sent was at: %s.", 
                    activity.sendingService.getDelayedIntentsCount(), lastSuccessfulSend == null ? "never" : lastSuccessfulSend));
            
            waitingView.setAdapter(new ArrayAdapter<String>(activity, 
                    android.R.layout.simple_list_item_1, 
                    activity.sendingService.getDelayedIntentsContent()));
        } else {
            statusView.setText(activity.getString(R.string.generic_error));
        }
    }

    private void setupPersistenceView() {
        // status and list view is updated by updateSendingServiceInformation
        Button clearButton = (Button) activity.findViewById(R.id.system_information_persistence_clear);
        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activity.boundSendingService) {
                    activity.sendingService.clearDelayedIntents();
                } else {
                    Toast.makeText(activity.getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupVersionView(String deviceIdentifier) {
        TextView identifierView = (TextView) activity.findViewById(R.id.system_information_application_identifier);
        identifierView.setText(deviceIdentifier);

        TextView versionView = (TextView) activity.findViewById(R.id.system_information_application_version);
        PackageInfo info = AppUtils.getPackageInfo(activity);
        if (info == null) {
            versionView.setText(activity.getString(R.string.generic_error));
        } else {
            versionView.setText(String.format("%s (Code %s)", info.versionName, info.versionCode));
        }
    }

    private void setupInstalledView() {
        TextView installView = (TextView) activity.findViewById(R.id.system_information_application_install);
        PackageInfo info = AppUtils.getPackageInfo(activity.getApplication());
        if (info == null) {
            installView.setText(activity.getString(R.string.generic_error));
        } else {
            Date installDate = new Date(info.lastUpdateTime);
            installView.setText(String.format("%s - %s", DateFormat.getLongDateFormat(activity).format(installDate),
                    DateFormat.getTimeFormat(activity).format(installDate)));
        }
    }
}
