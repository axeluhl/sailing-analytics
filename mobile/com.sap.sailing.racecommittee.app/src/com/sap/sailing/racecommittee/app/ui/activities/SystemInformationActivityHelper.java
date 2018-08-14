package com.sap.sailing.racecommittee.app.ui.activities;

import java.util.Date;

import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.android.shared.util.EulaHelper;
import com.sap.sailing.android.shared.util.LicenseHelper;
import com.sap.sailing.racecommittee.app.R;

import android.content.pm.PackageInfo;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.model.Notices;

public class SystemInformationActivityHelper {
    private final SendingServiceAwareActivity activity;

    public SystemInformationActivityHelper(SendingServiceAwareActivity activity, String deviceIdentifier) {
        this.activity = activity;
        activity.setContentView(R.layout.system_information_view);
        setupVersionView(deviceIdentifier);
        setupCompileView();
        setupInstalledView();
        setupPersistenceView();
        setupAboutButtons();
    }

    /**
     * Override the SendingServiceInformation with this content, and then call parent.
     */
    public void updateSendingServiceInformation() {
        TextView statusView = (TextView) activity.findViewById(R.id.system_information_persistence_status);
        TextView waitingView = (TextView) activity.findViewById(R.id.system_information_persistence_waiting);
        if (activity.boundSendingService) {
            Date lastSuccessfulSend = activity.sendingService.getLastSuccessfulSend();
            String never = activity.getString(R.string.never);
            statusView.setText(activity.getString(R.string.events_waiting_to_be_sent, activity.sendingService.getDelayedIntentsCount(), lastSuccessfulSend == null ? never : lastSuccessfulSend));

            Iterable<String> delayedIntentsContent = activity.sendingService.getDelayedIntentsContent();
            StringBuilder waitingEvents = new StringBuilder();
            synchronized (delayedIntentsContent) {
                boolean first = true;
                for (final String waitingEvent : delayedIntentsContent) {
                    if (!first) {
                        waitingEvents.append("\n");
                    } else {
                        first = false;
                    }
                    waitingEvents.append(waitingEvent);
                }
            }
            waitingView.setText(waitingEvents.toString());
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
        PackageInfo info = AppUtils.with(activity).getPackageInfo();
        if (info == null) {
            versionView.setText(activity.getString(R.string.generic_error));
        } else {
            versionView.setText(String.format("%s (Code %s)", info.versionName, info.versionCode));
        }
    }

    private void setupCompileView() {
        TextView view = (TextView) activity.findViewById(R.id.system_information_application_compile);
        if (view != null) {
            view.setText(AppUtils.with(activity).getBuildInfo());
        }
    }

    private void setupInstalledView() {
        TextView installView = (TextView) activity.findViewById(R.id.system_information_application_install);
        PackageInfo info = AppUtils.with(activity).getPackageInfo();
        if (info == null) {
            installView.setText(activity.getString(R.string.generic_error));
        } else {
            Date installDate = new Date(info.lastUpdateTime);
            installView.setText(String.format("%s - %s", DateFormat.getLongDateFormat(activity).format(installDate), DateFormat
                .getTimeFormat(activity).format(installDate)));
        }
    }

    private void setupAboutButtons() {
        Button eulaButton = (Button) activity.findViewById(R.id.eula_button);
        Button licenseButton = (Button) activity.findViewById(R.id.license_button);
        eulaButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EulaHelper.with(activity).openEulaPage();
            }
        });
        licenseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showLicenseDialog();
            }
        });
    }

    private void showLicenseDialog() {
        Notices notices = new Notices();
        LicenseHelper licenseHelper = new LicenseHelper();
        notices.addNotice(licenseHelper.getAndroidSupportNotice(activity));
        notices.addNotice(licenseHelper.getJsonSimpleNotice());
        notices.addNotice(licenseHelper.getDialogNotice(activity));
        LicensesDialog.Builder builder = new LicensesDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.license_information));
        builder.setThemeResourceId(R.style.AppTheme_AlertDialog);
        builder.setNotices(notices);
        builder.build().show();
    }
}
