package com.sap.sailing.racecommittee.app.ui.activities;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.list.ManagedRaceListFragment;

public class RacingActivity extends TwoPaneActivity /*implements ResetTimeListener, 
StartModeSelectionListener, PathfinderSelectionListener, GateLineOpeningTimeSelectionListener, CourseDesignSelectionListener*/ {
	private final static String TAG = RacingActivity.class.getName();
	
	private final static String ListFragmentTag = RacingActivity.class.getName() + ".ManagedRaceListFragment";
	
	/**
	 * Selection list of all races
	 */
	private ManagedRaceListFragment listFragment;
	
	/**
	 * Container fragment for currently selected race
	 */
	//private RaceInfoFragment infoFragment;
	
	private ReadonlyDataManager dataManager;
	
	@Override
	protected boolean onHomeClicked() {
		fadeActivity(LoginActivity.class);
		return true;
	}
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dataManager = DataManager.create(this);

		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.racing_view);
		setProgressBarIndeterminateVisibility(false);
		
		// on first create add race list fragment
		if (savedInstanceState == null) {
			addListFragment();
		} else {
			listFragment = (ManagedRaceListFragment) getFragmentManager().findFragmentByTag(ListFragmentTag);
		}
		
		Serializable courseAreaId = getCourseAreaIdFromIntent();
		if (courseAreaId == null) {
			throw new IllegalStateException("There was no course area id transmitted...");
		}
		setupTitle(courseAreaId);
		loadRaces(courseAreaId);
		//StaticVibrator.prepareVibrator(this);
		
	}

	private Serializable getCourseAreaIdFromIntent() {
		if (getIntent() == null || getIntent().getExtras() == null) {
			String msg = "Expected an intent carrying event extras.";
			Log.e(getClass().getName(), msg);
			Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			return null;
		}
		
		final Serializable courseId = getIntent().getExtras().getSerializable(AppConstants.COURSE_AREA_UUID_KEY);
		if (courseId == null) {
			String msg = "Expected an intent carrying the course area id.";
			Log.e(getClass().getName(), msg);
			Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			return null;
		}
		return courseId;
	}

	private void setupTitle(Serializable courseAreaId) {
		CourseArea courseArea = dataManager.getDataStore().getCourseArea(courseAreaId);
		if (courseArea == null) {
			throw new IllegalStateException("No course area with id found: " + courseAreaId);
		}
		
		TextView label = (TextView) findViewById(R.id.textListHeader);
		label.setText(String.format(getString(R.string.racingview_header), courseArea.getName()));
	}
	
	private void loadRaces(final Serializable courseAreaId) {
		setProgressBarIndeterminateVisibility(true);
		
		dataManager.loadRaces(courseAreaId, new LoadClient<Collection<ManagedRace>>() {
			public void onLoadSucceded(Collection<ManagedRace> data) {
				onLoadRacesSucceded(courseAreaId, data);

			}
			public void onLoadFailed(Exception reason) {
				onLoadRacesFailed(courseAreaId, reason);
			}
		});
	}
	
	private void onLoadRacesSucceded(Serializable courseAreaId, Collection<ManagedRace> data) {
		setProgressBarIndeterminateVisibility(false);

		dataManager.loadRaceLogs(data, new LoadClient<Map<ManagedRace,Collection<RaceLogEvent>>>() {
			
			public void onLoadSucceded(Map<ManagedRace, Collection<RaceLogEvent>> data) {
				Toast.makeText(RacingActivity.this, "OKOKOKOK", Toast.LENGTH_LONG).show();
			}
			
			public void onLoadFailed(Exception reason) {
				ExLog.w(TAG, "Loading of race logs failed:\n" + reason.toString());
			}
		});
		
		registerOnService(data);
		listFragment.setupOn(data);
		
		Toast.makeText(RacingActivity.this, 
				"Loaded "  + data.size() + " races for course " + courseAreaId.toString(), 
				Toast.LENGTH_LONG).show();
	}

	private void onLoadRacesFailed(final Serializable courseAreaId, Exception reason) {
		setProgressBarIndeterminateVisibility(false);
		showLoadFailedDialog(courseAreaId, reason.getMessage());
	}
	
	private void registerOnService(Collection<ManagedRace> races) {
		// since the service is the long-living component
		// he should decide whether these races are already
		// registered or not.
		for (ManagedRace race : races) {
	        Intent registerIntent = new Intent(AppConstants.REGISTER_RACE_ACTION);
	        registerIntent.putExtra(AppConstants.RACE_ID_KEY, race.getId());
	        this.startService(registerIntent);
		}
	}
	
	private void showLoadFailedDialog(final Serializable courseId, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this); 
		builder.setMessage(String.format("There was an error loading the requested data: %s\nDo you want to retry?", message))
			   .setTitle("Load failure")
			   .setIcon(R.drawable.ic_dialog_alert_holo_light)
		       .setCancelable(true) 
		       .setPositiveButton("Retry", new DialogInterface.OnClickListener() { 
		           public void onClick(DialogInterface dialog, int id) { 
		        	   loadRaces(courseId);
		           } 
		       }) 
		       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() { 
		           public void onClick(DialogInterface dialog, int id) { 
		        	   dialog.cancel(); 
		           } 
		       }); 
		AlertDialog alert = builder.create(); 
		alert.show(); 
	}
	
	public void onRaceItemClicked(ManagedRace race) {
		
	}

	/*public void onRaceItemClicked(ManagedRace race) {
		infoFragment = new RaceInfoFragment();
		Bundle args = new Bundle();
		args.putSerializable(AppConstants.RACE_UUID_KEY, race.getId());
		infoFragment.setArguments(args);
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
		transaction.replace(R.id.races_and_details_right, infoFragment);
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		transaction.commit();
		getRightLayout().setVisibility(View.VISIBLE);
	}*/
	
	private void addListFragment() {
		listFragment = new ManagedRaceListFragment();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.add(R.id.races_and_details_left, listFragment, ListFragmentTag);
		transaction.setTransition(FragmentTransaction.TRANSIT_NONE);
		transaction.commit();
	}

	/*public void onResetTimeClick() {
		infoFragment.displayResetTimeFragment();
	}

	public void onStartModeSelected() {
		infoFragment.startModeSelected();
	}


	public void onPathfinderSelected() {
		infoFragment.pathfinderSelected();
	}


	public void onLineOpeningTimeSelected() {
		infoFragment.gateLineOpeningTimeSelected();
	}


	@Override
	public void onCourseDesignSelected() {
		infoFragment.courseDesignSelected();
	}*/

}
