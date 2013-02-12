package com.sap.sailing.racecommittee.app.ui.adapters.racelist;

import android.app.Fragment;

import com.sap.sailing.domain.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public class RaceListDataTypeElement extends RaceListDataType {
	
	private boolean updateIndicatorVisible = false;
	
	private ManagedRace race;
	private RaceLogRaceStatus currentRaceStatus;
	
	private static String unscheduledTemplate;
	private static String scheduldedTemplate;
	private static String startPhaseTemplate;
	private static String runningTemplate;
	private static String finishedTemplate;
	private static String unknownTemplate;
	
	//private Format scheduleFormatter = new SimpleDateFormat("HH:mm", Locale.US);
	
	public static void initializeTemplates(Fragment fragment) {
		unscheduledTemplate = fragment.getString(R.string.racelist_unscheduled);
		scheduldedTemplate = fragment.getString(R.string.racelist_scheduled);
		startPhaseTemplate = fragment.getString(R.string.racelist_startphase);
		runningTemplate = fragment.getString(R.string.racelist_running);
		finishedTemplate = fragment.getString(R.string.racelist_finished);
		unknownTemplate = fragment.getString(R.string.racelist_unknown);
	}
	
	public RaceListDataTypeElement(ManagedRace race) {
		setRace(race);
	}
	
	public ManagedRace getRace() {
		return race;
	}

	public void setRace(ManagedRace race) {
		this.race = race;
		this.currentRaceStatus = race.getStatus();
	}
	
	public void setUpdateIndicator(boolean visible) {
		this.updateIndicatorVisible = visible;
	}
	
	public boolean isUpdateIndicatorVisible() {
		return updateIndicatorVisible;
	}
	
	public String getRaceName() {
		if (race == null) {
			return "Invalid race";
		}
		
		return race.getName();
	}
	
	public String getStatus() {
		if (race == null) {
			return "Invalid race";
		}
		
		return getStatusString(race.getStatus());
	}
	
	public RaceLogRaceStatus getPreviousRaceStatus() {
		return currentRaceStatus;
	}
	
	public void setPreviousRaceStatus(RaceLogRaceStatus status) {
		this.currentRaceStatus = status;
	}
	
	private String getStatusString(RaceLogRaceStatus status) {
		switch (status) {
		case UNSCHEDULED:
			return unscheduledTemplate;
		case SCHEDULED:
			return String.format(scheduldedTemplate, formatStartTime());
		case STARTPHASE:
			return String.format(startPhaseTemplate, formatStartTime());
		case RUNNING:
			return String.format(runningTemplate, formatStartTime());
		case FINISHED:
			return finishedTemplate;
		default:
			return unknownTemplate;
		}
	}

	private String formatStartTime() {
		//if (race.getStartTime() == null) 
		{
			return unknownTemplate;
		}
		//return scheduleFormatter.format(race.getStartTime());
	}

}
