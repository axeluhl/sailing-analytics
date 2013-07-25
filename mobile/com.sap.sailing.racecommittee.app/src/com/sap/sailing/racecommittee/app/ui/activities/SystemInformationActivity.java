package com.sap.sailing.racecommittee.app.ui.activities;

import java.util.Date;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;

import com.sap.sailing.racecommittee.app.R;

public class SystemInformationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_information_view);
        setupVersionView();
        setupInstalledView();
    }

    private void setupVersionView() {
        TextView versionView = (TextView) findViewById(R.id.system_information_application_version);
        PackageInfo info = getPackageInfo();
        if (info == null) {
            versionView.setText(getString(R.string.generic_error));
        } else {
            versionView.setText(String.format("%s (Code %s)", info.versionName, info.versionCode));
        }
    }

    private void setupInstalledView() {
        TextView installView = (TextView) findViewById(R.id.system_information_application_install);
        PackageInfo info = getPackageInfo();
        if (info == null) {
            installView.setText(getString(R.string.generic_error));
        } else {
            Date installDate = new Date(info.lastUpdateTime);
            installView.setText(String.format("%s - %s", DateFormat.getLongDateFormat(this).format(installDate),
                    DateFormat.getTimeFormat(this).format(installDate)));
        }
    }

    private PackageInfo getPackageInfo() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

}
