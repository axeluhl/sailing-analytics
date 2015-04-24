package com.sap.sailing.racecommittee.app.ui.fragments.preference;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;
import com.sap.sailing.android.shared.ui.fragments.preference.BasePreferenceFragment;
import com.sap.sailing.android.shared.ui.views.EditSetPreference;
import com.sap.sailing.domain.common.impl.DeviceConfigurationQRCodeUtils;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.BuildConfig;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.autoupdate.AutoUpdater;
import com.sap.sse.common.Util;

import java.net.MalformedURLException;
import java.net.URL;

public class GeneralPreferenceFragment extends BasePreferenceFragment {

    private static int requestCodeQRCode = 45392;
    
    private Preference identifierPreference;
    private Preference serverUrlPreference;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_general);
        
        setupConnection();
        setupPolling();
        setupGeneral();
        setupDeveloperOptions();
    }
    protected void setupGeneral() {
        setupLanguageButton();
        setupCourseAreasList();
        
        bindPreferenceSummaryToSet(findPreference(R.string.preference_course_areas_key));
        bindPreferenceSummaryToValue(findPreference(R.string.preference_mail_key));
        bindPreferenceToListEntry(findPreference(R.string.preference_theme_key), getString(R.string.preference_theme_default));
        addOnPreferenceChangeListener(findPreference(R.string.preference_theme_key), new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.AppTheme_AlertDialog);
                builder.setTitle(getString(R.string.theme_changed_title));
                builder.setMessage(R.string.theme_changed_message);
                builder.setPositiveButton(getString(android.R.string.ok),null);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });
    }

    private void setupPolling() {
        Preference intervalPreference = findPreference(R.string.preference_polling_interval_key);
        CheckBoxPreference activePreference = findPreference(R.string.preference_polling_active_key);
        bindPreferenceToCheckbox(activePreference, intervalPreference);
        bindPreferenceSummaryToInteger(intervalPreference);
    }

    protected void setupDeveloperOptions() {
        PreferenceScreen screen = getPreferenceScreen();
        PreferenceCategory category = (PreferenceCategory)findPreference(getString(R.string.preference_developer_key));
        if (!BuildConfig.DEBUG && screen != null && category != null) {
            screen.removePreference(category);
        }
    }

    protected void setupConnection() {
        setupIdentifierBox();
        setupServerUrlBox();
        setupSyncQRCodeButton();
        setupForceUpdateButton();

        bindPreferenceSummaryToValue(findPreference(R.string.preference_server_url_key));
    }

    private void setupIdentifierBox() {
        final AppPreferences appPreferences = AppPreferences.on(getActivity());
        identifierPreference = findPreference(R.string.preference_identifier_key);
        identifierPreference.setSummary(appPreferences.getDeviceIdentifier());
        addOnPreferenceChangeListener(identifierPreference, new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String value = (String) newValue;
                if (value.isEmpty()) {
                    preference.setSummary(appPreferences.getDeviceIdentifier());
                } else {
                    preference.setSummary(value);
                }
                return true;
            }
        });
    }

    private void setupServerUrlBox() {
        serverUrlPreference = findPreference(getString(R.string.preference_server_url_key));
        addOnPreferenceChangeListener(serverUrlPreference, new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(getActivity(), 
                        getString(R.string.settings_please_reload), Toast.LENGTH_LONG).show();
                return true;
            }
        });
    }

    private void setupSyncQRCodeButton() {
        Preference preference = findPreference(R.string.preference_sync_key);
        preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                requestQRCodeScan();
                return false;
            }
        });
    }

    private void setupForceUpdateButton() {
        Preference preference = findPreference(R.string.preference_update_key);
        preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AutoUpdater(getActivity()).checkForUpdate(true);
                return false;
            }
        });
    }

    private void setupCourseAreasList() {
        EditSetPreference preference = findPreference(R.string.preference_course_areas_key);
        // TODO: example values from DataStore
        preference.setExampleValues(getResources().getStringArray(R.array.preference_course_areas_example));
    }

    private void setupLanguageButton() {
        Preference preference = findPreference(R.string.preference_language_key);
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) { 
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("com.android.settings", "com.android.settings.LanguageSettings");            
                startActivity(intent);
                return true;
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
            try {
                Util.Pair<String, String> connectionConfiguration = DeviceConfigurationQRCodeUtils.splitQRContent(content);
                
                String identifier = connectionConfiguration.getA();
                URL apkUrl = tryConvertToURL(connectionConfiguration.getB());
                
                if (apkUrl != null) {
                    String serverUrl = getServerUrl(apkUrl);
                    
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    preferences.edit().putString(getString(R.string.preference_identifier_key), identifier).commit();
                    preferences.edit().putString(getString(R.string.preference_server_url_key), serverUrl).commit();
                    
                    identifierPreference.getOnPreferenceChangeListener().onPreferenceChange(identifierPreference, identifier);
                    serverUrlPreference.getOnPreferenceChangeListener().onPreferenceChange(serverUrlPreference, serverUrl);
                    
                    new AutoUpdater(getActivity()).checkForUpdate(false);
                } else {
                    String toastText = getString(R.string.error_scanning_qr_malformed);
                    Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
                }
            } catch (IllegalArgumentException e) {
                String toastText = getString(R.string.error_scanning_qr);
                toastText = String.format(toastText, e.getMessage());
                Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
            }
        } else {
            String toastText = getString(R.string.error_scanning_qr);
            toastText = String.format(toastText, "" + resultCode);
            Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
        }
    }

    protected String getServerUrl(URL apkUrl) {
        String protocol = apkUrl.getProtocol();
        String host = apkUrl.getHost();
        String port = apkUrl.getPort() == -1 ? "" : ":" + apkUrl.getPort();
        return protocol + "://" + host + port;
    }
    
    private URL tryConvertToURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
