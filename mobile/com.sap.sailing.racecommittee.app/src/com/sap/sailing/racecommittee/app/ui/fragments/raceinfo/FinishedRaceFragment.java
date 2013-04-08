/**
 * 
 */
package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.Date;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

/**
 * @author Klaus Trophie
 *
 */
public class FinishedRaceFragment extends RaceFragment {
	
	TextView headerView;
	TextView startTimeView;
	TextView firstBoatFinishedView;
	TextView finishTimeView;
	TextView protestStartTimeView;
	/// TODO: some time limit time is missing?! Dunno why it exists...
	
	@Override    
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment        
		return inflater.inflate(R.layout.race_finished_view, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		headerView = (TextView) getView().findViewById(R.id.textFinishedRace);
		startTimeView = (TextView) getView().findViewById(R.id.textFinishedRaceStarted);
		firstBoatFinishedView = (TextView) getView().findViewById(R.id.textFirstBoatFinished);
		finishTimeView = (TextView) getView().findViewById(R.id.textFinishedRaceEnded);
		protestStartTimeView = (TextView) getView().findViewById(R.id.textProtestStartTime);
		
		String raceFinishedText = String.format(String.valueOf(getText(R.string.race_finished_template)), getRace().getName());
		
		headerView.setText(raceFinishedText);
		
		Date startTime = new Date(); /// TODO: get real start time
		if (startTime != null) {
			startTimeView.setText(getString(R.string.race_finished_start_time) + " " + getFormattedTime(startTime));
		}
		
		Date firstBoatFinishedViewTime = new Date();/// TODO: get real first boat finished time
		if (firstBoatFinishedViewTime != null) {
			firstBoatFinishedView.setText(getString(R.string.race_first_boat_finished) + " " + getFormattedTime(firstBoatFinishedViewTime));
		}
		
		Date finishTime = new Date();/// TODO: get real finish time
		if (finishTime != null) {
			finishTimeView.setText(getString(R.string.race_finished_end_time) + " " + getFormattedTime(finishTime));
		}
		
		Date protestStartTime = new Date(); /// TODO: get real protest start time
		if (protestStartTime != null) {
			String raceProtestStartTimeText = String.format(String.valueOf(getText(R.string.protest_start_time)), getFormattedTime(protestStartTime));
			protestStartTimeView.setText(raceProtestStartTimeText);
			protestStartTimeView.setVisibility(View.VISIBLE);
		}
	}
	
	private String getFormattedTime(Date time) {
		return getFormattedTimePart(time.getHours()) + ":" + getFormattedTimePart(time.getMinutes()) + ":" + getFormattedTimePart(time.getSeconds());
	}
	private String getFormattedTimePart(int timePart) {
		return (timePart < 10) ? "0" + timePart : String.valueOf(timePart);
	}


}
