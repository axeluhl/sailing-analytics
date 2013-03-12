package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;

public class StartphaseRaceFragment extends RaceFragment implements TickListener {

	private RaceInfoListener infoListener;
	
	private TextView raceCountdown;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		if (activity instanceof RaceInfoListener) {
			this.infoListener = (RaceInfoListener) activity;
		} else {
			throw new UnsupportedOperationException(String.format(
					"%s must implement %s", 
					activity, 
					RaceInfoListener.class.getName()));
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.race_startphase_gen_view, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Button resetTimeButton = (Button) getView().findViewById(R.id.resetTimeButton);
		resetTimeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				infoListener.onResetTime();
			}
		});
		raceCountdown = (TextView) getView().findViewById(R.id.raceCountdown);
	}
	
	@Override
	public void onStart() {
		TickSingleton.INSTANCE.registerListener(this);
		super.onStart();
	}
	
	@Override
	public void onStop() {
		TickSingleton.INSTANCE.unregisterListener(this);
		super.onStop();
	}

	public void notifyTick() {
		TimePoint startTime = getRace().getState().getStartTime();
		if (startTime != null) {
			setCountdownLabels(TimeUtils.timeUntil(startTime));
		}
	}
	
	private void setCountdownLabels(long millisecondsTillStart) {
		setStarttimeCountdownLabel(millisecondsTillStart);
		//setNextFlagCountdownLabel(millisecondsTillStart);
	}

	private void setStarttimeCountdownLabel(long millisecondsTillStart) {
		raceCountdown.setText(String.format(
				getString(R.string.race_startphase_countdown_start),
				TimeUtils.prettyString(millisecondsTillStart), getRace().getName()));
	}
	
}
