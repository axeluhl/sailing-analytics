package com.sap.sailing.domain.base.racegroup;

import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sse.common.Util;

/**
 * Represents a pair of {@link RaceGroup} (representing a regatta or a flexible leaderboard) and {@link SeriesBase
 * series}. An instance can be constructed for a {@link ManagedRace} which then extracts these two properties from the
 * race. Note that equal objects of this type can result for different races as long as they are in the equal
 * {@link RaceGroup} and {@link SeriesBase series}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RaceGroupSeries {

    private final RaceGroup raceGroup;
    private final SeriesBase series;
    private final int seriesOrder;

    public RaceGroupSeries(FilterableRace race) {
        this(race.getRaceGroup(), race.getSeries());
    }

    public RaceGroupSeries(RaceGroup raceGroup, SeriesBase series) {
        this.raceGroup = raceGroup;
        this.series = series;
        seriesOrder = getSeriesIndex(raceGroup, series);
    }

    private static int getSeriesIndex(RaceGroup raceGroup, SeriesBase series) {
        return Util.indexOf(raceGroup.getSeries(), series);
    }

    public RaceGroup getRaceGroup() {
        return raceGroup;
    }

    public SeriesBase getSeries() {
        return series;
    }

    public String getRaceGroupName() {
        return raceGroup.getName();
    }

    public String getSeriesName() {
        return series.getName();
    }

    public String getDisplayName() {
        return getDisplayName(false);
    }

    public String getDisplayName(boolean useDisplayName) {
        String name = raceGroup.getDisplayName();
        if (!useDisplayName || name == null || name.length() == 0) {
            name = raceGroup.getName();
        }
        if (series != null && !series.getName().equals(LeaderboardNameConstants.DEFAULT_SERIES_NAME)) {
            name += " - " + series.getName();
        }
        return name;
    }

    public int getSeriesOrder() {
        return seriesOrder;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((raceGroup == null) ? 0 : raceGroup.hashCode());
        result = prime * result + ((series == null) ? 0 : series.hashCode());
        result = prime * result + seriesOrder;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RaceGroupSeries other = (RaceGroupSeries) obj;
        if (raceGroup == null) {
            if (other.raceGroup != null) {
                return false;
            }
        } else if (!raceGroup.equals(other.raceGroup)) {
            return false;
        }
        if (series == null) {
            if (other.series != null) {
                return false;
            }
        } else if (!series.equals(other.series)) {
            return false;
        }
        if (seriesOrder != other.seriesOrder) {
            return false;
        }
        return true;
    }

}
