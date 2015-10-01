package com.sap.sailing.racecommittee.app.utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.GateStartTimingFragment;

public class RaceHelper {
    public static String getRaceName(@Nullable ManagedRace race) {
        return getRaceName(race, null);
    }

    public static String getRaceName(@Nullable ManagedRace race, @Nullable String delimiter) {
        if (TextUtils.isEmpty(delimiter)) {
            delimiter = " - ";
        }

        String raceName = "";
        if (race != null) {
            raceName = getRaceGroupName(race);
            raceName += getSeriesName(race.getSeries(), delimiter);
            raceName += getFleetName(race.getFleet(), delimiter);
            raceName += delimiter + race.getRaceName();
        }

        return raceName;
    }

    public static String getRaceGroupName(@Nullable ManagedRace race) {
        String raceGroupName = "";
        if (race != null) {
            raceGroupName = getRaceGroupName(race.getRaceGroup());
        }

        return raceGroupName;
    }

    public static String getRaceGroupName(@Nullable RaceGroup raceGroup) {
        String raceGroupName = "";
        if (raceGroup != null) {
            raceGroupName = raceGroup.getDisplayName();
            if (TextUtils.isEmpty(raceGroupName)) {
                raceGroupName = raceGroup.getName();
            }
        }

        return raceGroupName;
    }

    public static String getFleetSeries(@Nullable ManagedRace race) {
        String fleetSeries = "";
        if (race != null) {
            fleetSeries = getFleetSeries(race.getFleet(), race.getSeries());
        }

        return fleetSeries;
    }

    public static String getFleetSeries(@Nullable Fleet fleet, @Nullable SeriesBase series) {
        return getFleetSeries(fleet, series, null);
    }

    public static String getFleetSeries(@Nullable Fleet fleet, @Nullable SeriesBase series, @Nullable String delimiter) {
        if (TextUtils.isEmpty(delimiter)) {
            delimiter = " - ";
        }

        String fleetSeries = "";
        fleetSeries += getFleetName(fleet, "");
        if (!TextUtils.isEmpty(fleetSeries)) {
            fleetSeries += delimiter;
        }
        fleetSeries += getSeriesName(series, "");

        return fleetSeries;
    }

    public static String getSeriesName(@Nullable SeriesBase series) {
        return getSeriesName(series, null);
    }

    public static String getSeriesName(@Nullable SeriesBase series, @Nullable String delimiter) {
        if (delimiter == null) {
            delimiter = " - ";
        }

        String seriesName = "";
        if (series != null && !LeaderboardNameConstants.DEFAULT_SERIES_NAME.equals(series.getName())) {
            seriesName = delimiter + series.getName();
        }

        return seriesName;
    }

    public static String getFleetName(@Nullable Fleet fleet) {
        return getFleetName(fleet, null);
    }

    public static String getFleetName(@Nullable Fleet fleet, @Nullable String delimiter) {
        if (delimiter == null) {
            delimiter = " - ";
        }

        String fleetName = "";
        if (fleet != null && !LeaderboardNameConstants.DEFAULT_FLEET_NAME.equals(fleet.getName())) {
            fleetName = delimiter + fleet.getName();
        }

        return fleetName;
    }

    public static String getGateTiming(Context context, GateStartRacingProcedure procedure) {
        String timing;
        long launchTime = procedure.getGateLaunchStopTime() / GateStartTimingFragment.ONE_MINUTE_MILLISECONDS;
        long golfTime = procedure.getGolfDownTime() / GateStartTimingFragment.ONE_MINUTE_MILLISECONDS;
        if (AppPreferences.on(context).getGateStartHasAdditionalGolfDownTime()) {
            timing = context.getString(R.string.gate_time_schedule_long, launchTime, golfTime, launchTime + golfTime);
        } else {
            timing = context.getString(R.string.gate_time_schedule_short, launchTime);
        }
        return timing;
    }
}
