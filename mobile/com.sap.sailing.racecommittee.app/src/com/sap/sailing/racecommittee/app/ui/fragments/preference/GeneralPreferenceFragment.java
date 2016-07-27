package com.sap.sailing.racecommittee.app.ui.fragments.preference;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.sap.sailing.android.shared.ui.fragments.preference.BasePreferenceFragment;
import com.sap.sailing.android.shared.ui.views.EditSetPreference;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.BuildConfig;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.ui.activities.PasswordActivity;
import com.sap.sailing.racecommittee.app.utils.QRHelper;
import com.sap.sailing.racecommittee.app.utils.autoupdate.AutoUpdater;

public class GeneralPreferenceFragment extends BasePreferenceFragment {

    private static int requestCodeQRCode = 45392;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_general);

        setupConnection();
        setupPolling();
        setupGeneral();
        setupDeveloperOptions();
    }

    private void setupGeneral() {
        setupLanguageButton();
        setupCourseAreasList();

        bindPreferenceSummaryToSet(findPreference(R.string.preference_course_areas_key));
        bindPreferenceSummaryToValue(findPreference(R.string.preference_mail_key));
        bindPreferenceToListEntry(findPreference(R.string.preference_theme_key), getString(R.string.preference_theme_default));
        addOnPreferenceChangeListener(findPreference(R.string.preference_theme_key), new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog);
                builder.setTitle(getString(R.string.theme_changed_title));
                builder.setMessage(R.string.theme_changed_message);
                builder.setPositiveButton(getString(android.R.string.ok), null);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });
        addOnPreferenceChangeListener(findPreference(R.string.preference_non_public_events_key), new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                AppPreferences.on(getActivity()).setNeedConfigRefresh(true);
                if (DataManager.create(getActivity()).getDataStore().getCourseUUID() != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog);
                    builder.setTitle(getString(R.string.non_public_changed_title));
                    builder.setMessage(getString(R.string.app_refresh_message));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                }
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

    private void setupDeveloperOptions() {
        PreferenceScreen screen = getPreferenceScreen();
        PreferenceCategory category = findPreference(R.string.preference_developer_key);
        if (!BuildConfig.DEBUG) {
            if (screen != null && category != null) {
                screen.removePreference(category);
            }
        }
    }

    private void setupConnection() {
        setupIdentifierBox();
        setupServerUrlBox();
        setupSyncQRCodeButton();
        setupForceUpdateButton();
        setupLogoutButton();
    }

    private void setupIdentifierBox() {
        final AppPreferences appPreferences = AppPreferences.on(getActivity());
        EditTextPreference identifierPreference = findPreference(R.string.preference_identifier_key);
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
        EditTextPreference serverUrlPreference = findPreference(R.string.preference_server_url_key);
        addOnPreferenceChangeListener(serverUrlPreference, new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                AppPreferences.on(getActivity()).setNeedConfigRefresh(true);
                if (DataManager.create(getActivity()).getDataStore().getCourseUUID() != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog);
                    builder.setTitle(getString(R.string.url_refresh_title));
                    builder.setMessage(getString(R.string.app_refresh_message));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                }
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

    private void setupLogoutButton() {
        Preference preference = findPreference(R.string.preference_logout_key);
        preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog)
                    .setTitle(R.string.logout_dialog_title)
                    .setMessage(getString(R.string.logout_dialog_message))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AppPreferences.on(getActivity()).setAccessToken(null);
                            startActivity(new Intent(getActivity(), PasswordActivity.class));
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
                return true;
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

    private boolean requestQRCodeScan() {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, requestCodeQRCode);
            return true;
        } catch (Exception e) {
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
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
            QRHelper.with(getActivity()).saveData(data.getStringExtra("SCAN_RESULT"));
            AppPreferences.on(getActivity()).setNeedConfigRefresh(true);
        } else {
            Toast.makeText(getActivity(), getString(R.string.error_scanning_qr, resultCode), Toast.LENGTH_LONG).show();
        }
    }
}
