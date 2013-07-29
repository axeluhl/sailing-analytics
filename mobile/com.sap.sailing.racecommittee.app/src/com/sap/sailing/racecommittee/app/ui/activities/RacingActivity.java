package com.sap.sailing.racecommittee.app.ui.activities;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceInfoFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.ManagedRaceListFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.ManagedRaceListFragment.FilterMode;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RaceInfoListener;

public class RacingActivity extends SessionActivity implements RaceInfoListener {
    // private final static String TAG = RacingActivity.class.getName();
    private final static String ListFragmentTag = RacingActivity.class.getName() + ".ManagedRaceListFragment";
    
    private static final String TAG = RacingActivity.class.getName();

    private class FilterModeSelectionBinder implements OnNavigationListener {

        private ManagedRaceListFragment targetList;
        private ActionBar actionBar;
        private List<FilterMode> items;

        public FilterModeSelectionBinder(ManagedRaceListFragment list, ActionBar actionBar,
                ArrayAdapter<FilterMode> adapter, FilterMode... rawItems) {
            this.targetList = list;
            this.actionBar = actionBar;
            this.items = Arrays.asList(rawItems);
            adapter.addAll(this.items);
            this.actionBar.setListNavigationCallbacks(adapter, this);
            trackSelection(list);
        }

        private void trackSelection(ManagedRaceListFragment list) {
            int selectedIndex = this.items.indexOf(list.getFilterMode());
            if (selectedIndex >= 0) {
                this.actionBar.setSelectedNavigationItem(selectedIndex);
            }
        }

        @Override
        public boolean onNavigationItemSelected(int position, long id) {
            targetList.setFilterMode(items.get(position));
            return true;
        }
    }

    /**
     * Selection list of all races
     */
    private ManagedRaceListFragment raceListFragment;

    /**
     * Container fragment for currently selected race
     */
    private RaceInfoFragment infoFragment;

    private ReadonlyDataManager dataManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // features must be requested before anything else
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        dataManager = DataManager.create(this);

        setContentView(R.layout.racing_view);
        setProgressBarIndeterminateVisibility(false);

        raceListFragment = getOrCreateRaceListFragment();

        Serializable courseAreaId = getCourseAreaIdFromIntent();
        if (courseAreaId == null) {
            throw new IllegalStateException("There was no course area id transmitted...");
        }
        CourseArea courseArea = dataManager.getDataStore().getCourseArea(courseAreaId);
        if (courseArea == null) {
            Toast.makeText(this, getString(R.string.racing_course_area_missing), Toast.LENGTH_LONG).show();
        } else {
            setupActionBar(courseArea);
            loadRaces(courseArea);
        }
    }
    
    @Override
    public void onBackPressed() {
        logoutSession();
    }

    private ManagedRaceListFragment getOrCreateRaceListFragment() {
        Fragment fragment = getFragmentManager().findFragmentByTag(ListFragmentTag);
        // on first create add race list fragment
        if (fragment == null) {
            fragment = createRaceListFragment();
        }
        return (ManagedRaceListFragment) fragment;
    }

    private Fragment createRaceListFragment() {
        Fragment fragment = new ManagedRaceListFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.racing_view_left_container, fragment, ListFragmentTag);
        transaction.setTransition(FragmentTransaction.TRANSIT_NONE);
        transaction.commit();
        return fragment;
    }

    private Serializable getCourseAreaIdFromIntent() {
        if (getIntent() == null || getIntent().getExtras() == null) {
            Log.e(getClass().getName(), "Expected an intent carrying event extras.");
            return null;
        }

        final Serializable courseId = getIntent().getExtras().getSerializable(AppConstants.COURSE_AREA_UUID_KEY);
        if (courseId == null) {
            Log.e(getClass().getName(), "Expected an intent carrying the course area id.");
            return null;
        }
        return courseId;
    }

    private void setupActionBar(CourseArea courseArea) {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(String.format(getString(R.string.racingview_header), courseArea.getName()));

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ArrayAdapter<ManagedRaceListFragment.FilterMode> adapter = new ArrayAdapter<ManagedRaceListFragment.FilterMode>(
                this, R.layout.action_bar_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        new FilterModeSelectionBinder(raceListFragment, actionBar, adapter, ManagedRaceListFragment.FilterMode.values());
    }

    private void loadRaces(final CourseArea courseArea) {
        setProgressBarIndeterminateVisibility(true);

        ExLog.i(TAG, "Issuing loading of managed races from data manager");
        dataManager.loadRaces(courseArea.getId(), new LoadClient<Collection<ManagedRace>>() {
            public void onLoadSucceded(Collection<ManagedRace> data) {
                onLoadRacesSucceded(courseArea, data);
            }

            public void onLoadFailed(Exception reason) {
                onLoadRacesFailed(courseArea, reason);
            }
        });
    }

    private void onLoadRacesSucceded(CourseArea courseArea, Collection<ManagedRace> data) {
        setProgressBarIndeterminateVisibility(false);

        registerOnService(data);
        raceListFragment.setupOn(data);

        Toast.makeText(RacingActivity.this, String.format(getString(R.string.racing_load_success), data.size()),
                Toast.LENGTH_SHORT).show();
    }

    private void onLoadRacesFailed(final CourseArea courseArea, Exception reason) {
        setProgressBarIndeterminateVisibility(false);
        showLoadFailedDialog(courseArea, reason.getMessage());
    }

    private void registerOnService(Collection<ManagedRace> races) {
        // since the service is the long-living component
        // he should decide whether these races are already
        // registered or not.
        for (ManagedRace race : races) {
            Intent registerIntent = new Intent(AppConstants.INTENT_ACTION_REGISTER_RACE);
            registerIntent.putExtra(AppConstants.RACE_ID_KEY, race.getId());
            this.startService(registerIntent);
        }
    }

    private void showLoadFailedDialog(final CourseArea courseArea, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(
                String.format(getString(R.string.generic_load_failure), message))
                .setTitle(getString(R.string.loading_failure)).setIcon(R.drawable.ic_dialog_alert_holo_light).setCancelable(true)
                .setPositiveButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loadRaces(courseArea);
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        if (!isFinishing()) {
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public void onRaceItemClicked(ManagedRace managedRace) {
        infoFragment = new RaceInfoFragment();
        infoFragment.setArguments(RaceFragment.createArguments(managedRace));

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
        transaction.replace(R.id.racing_view_right_container, infoFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    @Override
    public void onResetTime() {
        infoFragment.onResetTime();
    }

}
