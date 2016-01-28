package com.sap.sailing.racecommittee.app.ui.adapters.racelist;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Fragment;

import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.impl.RaceGroupSeriesFleet;
import com.sap.sse.common.TimePoint;

public class RaceListDataTypeRace implements RaceListDataType {

    private static String unscheduledTemplate;
    private static String prescheduledTemplate;
    private static String scheduldedTemplate;
    private static String startPhaseTemplate;
    private static String runningTemplate;
    private static String finishingTemplate;
    private static String finishedTemplate;
    private static String unknownTemplate;

    private boolean updateIndicatorVisible = false;
    private RaceLogRaceStatus currentStatus = RaceLogRaceStatus.UNKNOWN;
    private ManagedRace race;
    private RaceGroupSeriesFleet fleet;

    private Format scheduleFormatter = new SimpleDateFormat("HH:mm", Locale.US);

    public static void initializeTemplates(Fragment fragment) {
        unscheduledTemplate = fragment.getString(R.string.racelist_unscheduled);
        prescheduledTemplate = fragment.getString(R.string.racelist_prescheduled);
        scheduldedTemplate = fragment.getString(R.string.racelist_scheduled);
        startPhaseTemplate = fragment.getString(R.string.racelist_startphase);
        runningTemplate = fragment.getString(R.string.racelist_running);
        finishingTemplate = fragment.getString(R.string.racelist_finishing);
        finishedTemplate = fragment.getString(R.string.racelist_finished);
        unknownTemplate = fragment.getString(R.string.racelist_unknown);
    }

    public RaceListDataTypeRace(ManagedRace race) {
        this(race, null);
    }

    public RaceListDataTypeRace(ManagedRace race, RaceGroupSeriesFleet fleet) {
        this.race = race;
        this.fleet = fleet;
        this.currentStatus = race.getStatus();
    }

    public void onStatusChanged(RaceLogRaceStatus status, boolean allowUpdateIndicator) {
        if (!currentStatus.equals(status)) {
            currentStatus = status;
            setUpdateIndicatorVisible(allowUpdateIndicator);
        }
    }

    public void setUpdateIndicatorVisible(boolean visible) {
        this.updateIndicatorVisible = visible;
    }

    public boolean isUpdateIndicatorVisible() {
        return updateIndicatorVisible;
    }

    public String getRaceName() {
        return race.getName();
    }
    
    public ManagedRace getRace() {
        return race;
    }

    public RaceGroupSeriesFleet getFleet() { return fleet; }

    public RaceLogRaceStatus getCurrentStatus() {
        return currentStatus;
    }

    public String getStatusText() {
        return getStatusString(currentStatus);
    }

    private String getStatusString(RaceLogRaceStatus status) {
        switch (status) {
        case UNSCHEDULED:
            return unscheduledTemplate;
        case PRESCHEDULED:
            return String.format(prescheduledTemplate, formatStartTime());
        case SCHEDULED:
            return String.format(scheduldedTemplate, formatStartTime());
        case STARTPHASE:
            return String.format(startPhaseTemplate, formatStartTime());
        case RUNNING:
            return String.format(runningTemplate, formatStartTime());
        case FINISHING:
            return finishingTemplate;
        case FINISHED:
            return finishedTemplate;
        default:
            return unknownTemplate;
        }
    }

    private String formatStartTime() {
        TimePoint startTime = race.getState().getStartTime(); 
        if (startTime == null) 
        {
            return unknownTemplate;
        }
        return scheduleFormatter.format(startTime.asDate());
    }

}
