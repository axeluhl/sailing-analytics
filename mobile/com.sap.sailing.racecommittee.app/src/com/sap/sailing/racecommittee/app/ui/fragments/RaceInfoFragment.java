package com.sap.sailing.racecommittee.app.ui.fragments;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.chooser.RaceInfoFragmentChooser;


public class RaceInfoFragment extends RaceFragment {
	private final static String TAG = RaceInfoFragment.class.getName();

	private RaceInfoFragmentChooser infoFragmentChooser;
	private RaceFragment infoFragment;
	
	private TextView fleetInfoHeader;
	private TextView raceInfoHeader;
	private TextView courseInfoHeader;
	
	public RaceInfoFragment() {
		this.infoFragmentChooser = new RaceInfoFragmentChooser();
		this.infoFragment = null;	// will be set later by switchToInfoFragment()
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.race_info_view, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		this.fleetInfoHeader = (TextView) getView().findViewById(R.id.regattaGroupInfoHeader);
		this.raceInfoHeader = (TextView) getView().findViewById(R.id.raceInfoHeader);
		this.courseInfoHeader = (TextView) getView().findViewById(R.id.courseInfoHeader);
		
		courseInfoHeader.setText(getString(R.string.running_on_unknown));
		fleetInfoHeader.setText(String.format("%s - %s", 
				getRace().getRaceGroup().getName(), 
				getRace().getFleet().getName()));
		raceInfoHeader.setText(String.format("%s", getRace().getName()));
		
		/// TODO: implement course selection
		
		/// TODO: implement reset button
		Button resetButton = ((Button) getView().findViewById(R.id.btnResetRace));
		resetButton.setText("No yet");
		resetButton.setActivated(false);
		
		// Initial fragment selection...
		switchToInfoFragment();
	}

	public RaceFragment getInfoFragment() {
		return infoFragment;
	}

	protected void switchToInfoFragment() {
		switchToInfoFragment(infoFragmentChooser.choose(getRace()));
	}

	protected void switchToInfoFragment(RaceFragment choosenFragment) {
		ExLog.i(TAG, String.format("Choosed a %s fragment for race %s with status %s", 
				choosenFragment.getClass().getName(), 
				getRace().getId(), 
				getRace().getStatus()));
		
		this.infoFragment = choosenFragment;
		displayInfoFragment();
	}

	private void displayInfoFragment() {
		FragmentTransaction transaction = getFragmentManager()
				.beginTransaction();
		transaction.setCustomAnimations(R.animator.slide_in,
				R.animator.slide_out);
		transaction.replace(R.id.infoContainer, infoFragment);
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		transaction.commit();
	}
	
}
