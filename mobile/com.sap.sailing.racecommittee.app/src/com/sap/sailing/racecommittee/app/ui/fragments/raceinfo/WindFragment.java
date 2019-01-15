package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.base.racegroup.CurrentRaceComparator;
import com.sap.sailing.domain.base.racegroup.CurrentRaceFilter;
import com.sap.sailing.domain.base.racegroup.RaceGroupSeriesFleet;
import com.sap.sailing.domain.base.racegroup.impl.CurrentRaceFilterImpl;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.utils.OnRaceUpdatedListener;
import com.sap.sailing.racecommittee.app.ui.views.AccuracyView;
import com.sap.sailing.racecommittee.app.ui.views.CompassView;
import com.sap.sailing.racecommittee.app.ui.views.CompassView.CompassDirectionListener;
import com.sap.sailing.racecommittee.app.utils.DecimalInputTextWatcher;
import com.sap.sailing.racecommittee.app.utils.GeoUtils;
import com.sap.sailing.racecommittee.app.utils.RaceHelper;
import com.sap.sailing.racecommittee.app.utils.RangeInputFilter;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sailing.racecommittee.app.utils.WindHelper;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class WindFragment extends BaseFragment
        implements CompassDirectionListener, OnRaceUpdatedListener,
        OnSuccessListener<LocationSettingsResponse>, OnFailureListener {

    private final static String TAG = WindFragment.class.getName();
    private final static long FIVE_SEC = 5000;
    private final static long EVERY_POSITION_CHANGE = 2000;
    private final static int MIN_KTS = 3;
    private final static int MAX_KTS = 30;
    private final static int REQUEST_PERMISSIONS_REQUEST_CODE = 42;
    private final static int REQUEST_CHECK_SETTINGS = 43;

    private View mHeaderLayout;
    private View mContentLayout;

    private TextView mHeaderText;
    private TextView mHeaderWindSensor;
    private Button mSetData;
    private Button mSetDataMulti;
    private CompassView mCompassView;
    private NumberPicker mWindSpeed;
    private TextView mLatitude;
    private TextView mLongitude;
    private AccuracyView mAccuracy;
    private TextView mAccuracyTimestamp;
    private EditText mWindInputDirection;
    private EditText mWindInputSpeed;
    private Button mContentMapShow;
    private ImageView mEditCourse;
    private ImageView mEditSpeed;

    private FusedLocationProviderClient apiClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private Location mCurrentLocation;
    private LocationCallback mLocationCallback;

    private IsTrackedReceiver mReceiver;

    private List<ManagedRace> mSelectedRaces;
    private List<ManagedRace> mManagedRaces;
    private LinkedHashMap<RaceGroupSeriesFleet, List<ManagedRace>> mRacesByGroup;

    public static WindFragment newInstance(@START_MODE_VALUES int startMode) {
        WindFragment fragment = new WindFragment();
        Bundle args = new Bundle();
        args.putInt(START_MODE, startMode);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * creates the string array that represents the numbers in the wind speed number picker
     *
     * @return numbers for wind speed number picker
     */
    private String[] generateNumbers() {
        ArrayList<String> numbers = new ArrayList<>();
        String kn = getString(R.string.wind_kn);
        for (float i = MIN_KTS; i <= MAX_KTS; i += .5f) {
            numbers.add(String.format(Locale.US, "%.1f ", i) + kn);
        }
        return numbers.toArray(new String[numbers.size()]);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize the googleApiClient for location requests
        apiClient = LocationServices.getFusedLocationProviderClient(requireContext());
        settingsClient = LocationServices.getSettingsClient(requireContext());
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(FIVE_SEC);
        locationRequest.setFastestInterval(EVERY_POSITION_CHANGE);
        locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mCurrentLocation = locationResult.getLastLocation();
                refreshUI(false);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.wind_view, container, false);

        mHeaderLayout = ViewHelper.get(layout, R.id.header_layout);
        mContentLayout = ViewHelper.get(layout, R.id.content_layout);

        mHeaderText = ViewHelper.get(layout, R.id.header_text);
        mHeaderWindSensor = ViewHelper.get(layout, R.id.wind_sensor);
        // disabled, because of bug #2871
        // mWindOff = ViewHelper.get(layout, R.id.wind_off);
        // mWindOn = ViewHelper.get(layout, R.id.wind_on);
        mSetData = ViewHelper.get(layout, R.id.set_data);
        mSetDataMulti = ViewHelper.get(layout, R.id.set_data_multi);
        mCompassView = ViewHelper.get(layout, R.id.compass_view);
        mWindSpeed = ViewHelper.get(layout, R.id.wind_speed);
        mLatitude = ViewHelper.get(layout, R.id.latitude_value);
        mLongitude = ViewHelper.get(layout, R.id.longitude_value);
        mAccuracy = ViewHelper.get(layout, R.id.accuracy_value);
        mAccuracyTimestamp = ViewHelper.get(layout, R.id.accuracy_timestamp);
        mWindInputDirection = ViewHelper.get(layout, R.id.wind_input_direction);
        if (mWindInputDirection != null) {
            mWindInputDirection.setFilters(new InputFilter[] { new RangeInputFilter(0, 360) });
        }
        mWindInputSpeed = ViewHelper.get(layout, R.id.wind_input_speed);
        if (mWindInputSpeed != null) {
            mWindInputSpeed.setFilters(new InputFilter[] { new RangeInputFilter(0, MAX_KTS) });
            mWindInputSpeed.addTextChangedListener(new DecimalInputTextWatcher(mWindInputSpeed, 1));
        }
        mContentMapShow = ViewHelper.get(layout, R.id.position_show);

        mReceiver = new IsTrackedReceiver(mContentMapShow);

        mEditCourse = ViewHelper.get(layout, R.id.edit_course);
        if (mEditCourse != null) {
            mEditCourse.setOnClickListener(new EditCourseClick());
        }
        mEditSpeed = ViewHelper.get(layout, R.id.edit_speed);
        if (mEditSpeed != null) {
            mEditSpeed.setOnClickListener(new EditSpeedClick());
        }

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mHeaderWindSensor != null && getRace() != null && getRaceState() != null
                && getRaceState().getWindFix() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", getResources().getConfiguration().locale);
            Wind wind = getRaceState().getWindFix();
            mHeaderWindSensor.setText(getString(R.string.wind_sensor, dateFormat.format(wind.getTimePoint().asDate()),
                    wind.getFrom().getDegrees(), wind.getKnots()));
        }
        setupButtons();
        setupWindSpeedPicker();
        setupLayouts(false);

        refreshUI(false);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mCompassView != null) {
            mCompassView.setDirectionListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // disconnect googleApiClient and unregister position poller
        stopLocationUpdates();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mReceiver);
        sendIntent(AppConstants.INTENT_ACTION_TIME_SHOW);
    }

    @Override
    public void onResume() {
        super.onResume();
        mRacesByGroup = ((RacingActivity) getActivity()).getRacesByGroup();
        final Set<ManagedRace> allRaces = new HashSet<>();
        for (final Iterable<ManagedRace> racesInGroup : mRacesByGroup.values()) {
            Util.addAll(racesInGroup, allRaces);
        }
        final CurrentRaceFilter<ManagedRace> raceFilter = new CurrentRaceFilterImpl<>(allRaces);
        mManagedRaces = new ArrayList<>(raceFilter.getCurrentRaces());
        Collections.sort(mManagedRaces, new CurrentRaceComparator());
        mSelectedRaces = new ArrayList<>();
        sendIntent(AppConstants.INTENT_ACTION_TIME_HIDE);
        // Check the location settings
        checkLocationSettings();
        // register receiver to be notified if race is tracked
        IntentFilter filter = new IntentFilter(AppConstants.INTENT_ACTION_IS_TRACKING);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);
        // Contact server and ask if race is tracked and map is allowed to show.
        WindHelper.isTrackedRace(getActivity(), getRace());
        sendIntent(AppConstants.INTENT_ACTION_TIME_HIDE);
    }

    @Override
    public void notifyTick(TimePoint now) {
        super.notifyTick(now);

        refreshUI(true);
    }

    /**
     * refresh location data labels and highlight missing or inaccuracy gps data disable setData button if gps data is
     * missing or inaccurate
     *
     * @param timeOnly
     *            updates only the time since last position
     */
    private void refreshUI(boolean timeOnly) {
        if (isAdded()) {
            int whiteColor = ThemeHelper.getColor(getActivity(), R.attr.white);
            int redColor = R.color.sap_red;
            if (!timeOnly) {
                setTextAndColor(mLatitude, getString(R.string.not_available), redColor);
                setTextAndColor(mLongitude, getString(R.string.not_available), redColor);
                setTextAndColor(mAccuracyTimestamp, null, whiteColor);
                if (mAccuracy != null) {
                    mAccuracy.setAccuracy(-1);
                }
            }
            if (!timeOnly) {
                mSetData.setEnabled(mCurrentLocation != null);
                mSetDataMulti.setEnabled(mCurrentLocation != null);
            }
            if (mCurrentLocation != null) {
                if (!timeOnly) {
                    double latitude = mCurrentLocation.getLatitude();
                    double longitude = mCurrentLocation.getLongitude();
                    if (mAccuracy != null) {
                        mAccuracy.setAccuracy(mCurrentLocation.getAccuracy());
                    }
                    setTextAndColor(mLatitude, GeoUtils.getInDMSFormat(getActivity(), latitude), whiteColor);
                    setTextAndColor(mLongitude, GeoUtils.getInDMSFormat(getActivity(), longitude), whiteColor);
                }
                long timeDifference = System.currentTimeMillis() - mCurrentLocation.getTime();
                setTextAndColor(mAccuracyTimestamp,
                        getString(R.string.accuracy_timestamp, TimeUtils.formatTimeAgo(getActivity(), timeDifference)),
                        whiteColor);
            }
        }
    }

    private void setTextAndColor(TextView textView, String text, int color) {
        if (textView != null) {
            textView.setText(text);
            textView.setTextColor(color);
        }
    }

    /**
     * Determines if location settings are adequate.
     * If they are not, begins the process of presenting a location settings dialog to the user.
     */
    private void checkLocationSettings() {
        if (hasPermissions()) {
            settingsClient.checkLocationSettings(locationSettingsRequest)
                    .addOnSuccessListener(this)
                    .addOnFailureListener(this);
        } else {
            requestPermissions();
        }
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    @SuppressWarnings("MissingPermission")
    private void startLocationUpdates() {
        apiClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
        apiClient.getLastLocation();
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        apiClient.removeLocationUpdates(mLocationCallback);
    }

    /**
     * configures all the buttons in the view
     */
    public void setupButtons() {
        if (mHeaderText != null) {
            mHeaderText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goHome();
                }
            });
        }
        if (mSetData != null) {
            mSetData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSendClick();
                }
            });
        }
        if (mSetDataMulti != null) {
            mSetDataMulti.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showWindDialog();
                }
            });
        }
        if (mContentMapShow != null) {
            // disable functionality at first. will be enabled after contacting the server if race is tracked
            mContentMapShow.setEnabled(false);
            mContentMapShow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadRaceMap(/* showWindCharts */ true, /* showStreamlets */ false, /* showSimulation */ false,
                            /* showMapControls */ true);
                }
            });
        }
    }

    /**
     * configures the wind speed picker views and attaches all relevant listener functions to them
     */
    public void setupWindSpeedPicker() {
        double enteredWindSpeed = preferences.getWindSpeed();
        double enteredWindBearingFrom = preferences.getWindBearingFromDirection();
        if (getRace() != null && getRaceState() != null) {
            Wind enteredWind = getRaceState().getWindFix();
            if (enteredWind != null) {
                enteredWindSpeed = enteredWind.getKnots();
                enteredWindBearingFrom = enteredWind.getFrom().getDegrees();
            }
        }

        if (mWindSpeed != null && mCompassView != null) {
            initSpeedPicker(mWindSpeed, ThemeHelper.getColor(getActivity(), R.attr.white));
            mCompassView.setDirection((float) enteredWindBearingFrom);
            mWindSpeed.setValue(((int) ((enteredWindSpeed - MIN_KTS) * 2)));
        } else if (mWindInputDirection != null && mWindInputSpeed != null) {
            mWindInputDirection.setText(String.valueOf((int) enteredWindBearingFrom));
            mWindInputSpeed.setText(String.format(Locale.US, "%.1f", enteredWindSpeed));
        }
    }

    private void initSpeedPicker(NumberPicker picker, @ColorInt int textColor) {
        String numbers[] = generateNumbers();
        ViewHelper.disableSave(picker);
        ThemeHelper.setPickerColor(getActivity(), picker, textColor,
                ThemeHelper.getColor(getActivity(), R.attr.sap_yellow_1));
        picker.setMaxValue(numbers.length - 1);
        picker.setMinValue(0);
        picker.setWrapSelectorWheel(false);
        picker.setDisplayedValues(numbers);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
    }

    // the map view needs java script
    @SuppressLint("SetJavaScriptEnabled")
    private void setupLayouts(boolean showMap) {
        if (mHeaderLayout != null) {
            if (getArguments() != null
                    && getArguments().getInt(START_MODE, START_MODE_PRESETUP) == START_MODE_PLANNED) {
                if (AppUtils.with(getActivity()).isLandscape()) {
                    mHeaderLayout.setVisibility(View.GONE);
                }
            } else {
                mHeaderLayout.setVisibility(showMap ? View.GONE : View.VISIBLE);
            }
        }
        if (mContentLayout != null) {
            mContentLayout.setVisibility(showMap ? View.GONE : View.VISIBLE);
        }
    }

    private boolean loadRaceMap(boolean showWindCharts, boolean showStreamlets, boolean showSimulation,
            boolean showMapControls) {
        ManagedRace race = getRace();
        if (race != null) {
            // build complete race map url
            String mapUrl = WindHelper.generateMapURL(getActivity(), race, showWindCharts, showStreamlets,
                    showSimulation, showMapControls);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(mapUrl));
            startActivity(intent);
            return true;
        }
        return false;
    }

    /**
     * sends the entered windData to the server
     */
    private void onSendClick() {
        Wind wind = getResultingWindFix();
        boolean isMagnetic = preferences.isMagnetic();
        getRaceState().setWindFix(MillisecondsTimePoint.now(), wind, isMagnetic);
        saveEntriesInPreferences(wind);
        switch (getArguments().getInt(START_MODE, 0)) {
        case 1:
            break;
        default:
            openMainScheduleFragment();
            break;
        }
    }

    @Override
    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
        startLocationUpdates();
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        int statusCode = ((ApiException) e).getStatusCode();
        if (statusCode == CommonStatusCodes.RESOLUTION_REQUIRED) {
            try {
                ResolvableApiException resolvable = (ResolvableApiException) e;
                resolvable.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS);
            } catch (IntentSender.SendIntentException ignored) {
            }
        }
    }

    @Override
    public void onDirectionChanged(float degree) {
    }

    @Override
    public void OnRaceUpdated(ManagedRace race) {
    }

    private Wind getResultingWindFix() throws NumberFormatException {
        double windSpeed = 0;
        double windBearing = 0;
        Position currentPosition = new DegreePosition(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        if (mCompassView != null && mWindSpeed != null) {
            windBearing = mCompassView.getDirection();
            windSpeed = mWindSpeed.getValue() / 2 + MIN_KTS;
        } else if (mWindInputDirection != null && mWindInputSpeed != null) {
            windBearing = Double.parseDouble(mWindInputDirection.getText().toString());
            windSpeed = Double.parseDouble(mWindInputSpeed.getText().toString());
        }
        Bearing bearing_from = new DegreeBearingImpl(windBearing);
        SpeedWithBearing speedBearing = new KnotSpeedWithBearingImpl(windSpeed, bearing_from.reverse());
        return new WindImpl(currentPosition, MillisecondsTimePoint.now(), speedBearing);
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    /**
     * saves the last entered wind in the preferences, so next time wind has to be entered those saved presets get
     * loaded ( unless there was a wind entered for that race already )
     *
     * @param wind
     *            the wind to save
     */
    protected void saveEntriesInPreferences(Wind wind) {
        preferences.setWindBearingFromDirection(wind.getBearing().reverse().getDegrees());
        preferences.setWindSpeed(wind.getKnots());
    }

    private void showWindDialog() {
        ArrayList<String> races = new ArrayList<>();
        for (ManagedRace race : mManagedRaces) {
            races.add(RaceHelper.getShortReverseRaceName(race, " / ", getRace()));
        }
        mSelectedRaces.clear();
        mSelectedRaces.addAll(RaceHelper.getPreSelectedRaces(mRacesByGroup, getRace()));
        boolean[] selected = new boolean[mManagedRaces.size()];
        int i = 0;
        for (final ManagedRace r : mManagedRaces) {
            selected[i++] = mSelectedRaces.contains(r);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.wind_select_race));
        builder.setMultiChoiceItems(races.toArray(new String[races.size()]), selected,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        ManagedRace race = mManagedRaces.get(which);
                        if (mSelectedRaces.contains(race)) {
                            mSelectedRaces.remove(race);
                        }
                        if (isChecked) {
                            mSelectedRaces.add(race);
                        }
                    }
                });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (ManagedRace race : mSelectedRaces) {
                    race.getState().setWindFix(MillisecondsTimePoint.now(), getResultingWindFix(),
                            preferences.isMagnetic());
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private static class IsTrackedReceiver extends BroadcastReceiver {

        private WeakReference<Button> reference;

        public IsTrackedReceiver(Button button) {
            reference = new WeakReference<>(button);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Button button = reference.get();
            if (button != null) {
                button.setEnabled(intent.getBooleanExtra(AppConstants.INTENT_ACTION_IS_TRACKING_EXTRA, false));
            }
        }
    }

    private class EditCourseClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(R.string.wind_from);
            View layout = getActivity().getLayoutInflater().inflate(R.layout.wind_input_course, null);
            final CompassView compassView = (CompassView) layout.findViewById(R.id.compass_view);
            if (compassView != null) {
                if (mWindInputDirection != null && !TextUtils.isEmpty(mWindInputDirection.getText())) {
                    compassView.setDirection(Float.valueOf(mWindInputDirection.getText().toString()));
                } else {
                    compassView.setDirection(0);
                }
                compassView.setReadOnly(true);
            }
            builder.setView(layout);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (compassView != null) {
                        float degrees = (compassView.getDirection() >= 0) ? compassView.getDirection()
                                : compassView.getDirection() + 360;
                        mWindInputDirection.setText(String.format("%.0f", degrees));
                    }
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        }
    }

    private class EditSpeedClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(R.string.wind_speed);
            View layout = getActivity().getLayoutInflater().inflate(R.layout.wind_input_speed, null);
            final NumberPicker speed = (NumberPicker) layout.findViewById(R.id.wind_speed);
            initSpeedPicker(speed, getResources().getColor(R.color.black));
            double value = 0;
            if (!TextUtils.isEmpty(mWindInputSpeed.getText())) {
                value = Double.valueOf(mWindInputSpeed.getText().toString());
            }
            speed.setValue(((int) ((value - MIN_KTS) * 2)));
            builder.setView(layout);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    float value = (float) speed.getValue() / 2 + MIN_KTS;
                    mWindInputSpeed.setText(String.valueOf(value));
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        }
    }
}