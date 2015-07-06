package com.sap.sailing.racecommittee.app.utils;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

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
            raceName = race.getRaceGroup().getDisplayName();
            if (TextUtils.isEmpty(raceName)) {
                raceName = race.getRaceGroup().getName();
            }

            raceName += getSeriesName(race.getSeries(), delimiter);
            raceName += getFleetName(race.getFleet(), delimiter);
            raceName += delimiter + race.getRaceName();
        }
        return raceName;
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
}
