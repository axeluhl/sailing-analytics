package com.sap.sailing.racecommittee.app.ui.fragments.preference;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.ui.activities.BarcodeCaptureActivity;
import com.sap.sailing.android.shared.ui.fragments.preference.BasePreferenceFragment;
import com.sap.sailing.android.shared.ui.views.EditSetPreference;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.BuildConfig;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.PreferencesDeviceConfigurationLoader;
import com.sap.sailing.racecommittee.app.utils.QRHelper;
import com.sap.sailing.racecommittee.app.utils.autoupdate.AutoUpdater;

import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class GeneralPreferenceFragment extends BasePreferenceFragment {

    private static final String TAG = GeneralPreferenceFragment.class.getName();

    private static int REQUEST_CODE_QR_CODE = 45392;

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
        addOnPreferenceChangeListener(findPreference(R.string.preference_non_public_events_key),
                new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        AppPreferences.on(getActivity()).setNeedConfigRefresh(true);
                        if (DataManager.create(getActivity()).getDataStore().getCourseAreaId() != null) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
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
    }

    private void setupIdentifierBox() {
        final AppPreferences appPreferences = AppPreferences.on(getActivity());
        EditTextPreference identifierPreference = findPreference(R.string.preference_identifier_key);
        identifierPreference.setSummary(appPreferences.getDeviceConfigurationName());
        addOnPreferenceChangeListener(identifierPreference, new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String value = (String) newValue;
                if (value.isEmpty()) {
                    preference.setSummary(appPreferences.getDeviceConfigurationName());
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
                if (DataManager.create(getActivity()).getDataStore().getCourseAreaId() != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
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

    private void requestQRCodeScan() {
        Intent intent = new Intent(getContext(), BarcodeCaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE_QR_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_QR_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS && data != null) {
                Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                QRHelper.with(getActivity()).saveData(barcode.displayValue);
                final AppPreferences appPreferences = AppPreferences.on(getActivity());
                appPreferences.setNeedConfigRefresh(true);
                // update device identifier in ui
                EditTextPreference identifierPreference = findPreference(R.string.preference_identifier_key);
                identifierPreference.setText(appPreferences.getDeviceConfigurationName());
                identifierPreference.setSummary(appPreferences.getDeviceConfigurationName());
                // update server url in ui
                EditTextPreference serverUrlPreference = findPreference(R.string.preference_server_url_key);
                serverUrlPreference.setText(appPreferences.getServerBaseURL());

                //Update all the other preferences, e.g. course areas, mail recipient
                final SharedPreferences.OnSharedPreferenceChangeListener listener =
                        new SharedPreferences.OnSharedPreferenceChangeListener() {
                            @SuppressWarnings("unchecked")
                            @Override
                            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                                final Preference preference = findPreference(key);
                                if (preference == null) {
                                    return;
                                }
                                final Object newValue = sharedPreferences.getAll().get(key);
                                final OnPreferenceChangeListener listener = preference.getOnPreferenceChangeListener();
                                if (listener != null) {
                                    listener.onPreferenceChange(preference, newValue);
                                }
                                if (preference instanceof EditSetPreference) {
                                    preference.persistStringSet((Set<String>) newValue);
                                }
                            }
                        };

                PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext()))
                        .registerOnSharedPreferenceChangeListener(listener);

                String deviceConfigurationName = AppPreferences.on(getActivity()).getDeviceConfigurationName();
                UUID deviceConfigurationUuid = AppPreferences.on(getActivity()).getDeviceConfigurationUuid();
                LoaderManager.LoaderCallbacks<?> configurationLoader = DataManager.create(getActivity())
                        .createConfigurationLoader(deviceConfigurationName, deviceConfigurationUuid, new LoadClient<DeviceConfiguration>() {

                            @Override
                            public void onLoadFailed(Exception reason) {
                                if (reason instanceof FileNotFoundException) {
                                    Toast.makeText(getActivity(),
                                            getString(R.string.loading_configuration_not_found),
                                            Toast.LENGTH_LONG).show();
                                    ExLog.w(getActivity(), TAG, String.format(
                                            "There seems to be no configuration for this device: %s",
                                            reason.toString()));
                                } else {
                                    Toast.makeText(getActivity(),
                                            getString(R.string.loading_configuration_failed),
                                            Toast.LENGTH_LONG).show();
                                    ExLog.ex(getActivity(), TAG, reason);
                                }

                                PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext()))
                                        .unregisterOnSharedPreferenceChangeListener(listener);
                            }

                            @Override
                            public void onLoadSucceeded(DeviceConfiguration configuration,
                                                        boolean isCached) {
                                getLoaderManager().destroyLoader(0);

                                // this is our 'global' configuration, let's store it in app preferences
                                PreferencesDeviceConfigurationLoader
                                        .wrap(configuration, AppPreferences.on(getActivity())).store();

                                Toast.makeText(getActivity(),
                                        getString(R.string.loading_configuration_succeded),
                                        Toast.LENGTH_LONG).show();

                                PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext()))
                                        .unregisterOnSharedPreferenceChangeListener(listener);
                            }
                        });

                getLoaderManager().initLoader(0, null, configurationLoader).forceLoad();
            } else {
                Toast.makeText(getActivity(), getString(R.string.error_scanning_qr, resultCode), Toast.LENGTH_LONG).show();
            }
        }
    }
}
