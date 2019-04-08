package com.sap.sailing.racecommittee.app.domain.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.FinishingTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinder;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.racegroup.RaceCell;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.AndroidRaceLogResolver;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.ManagedRaceIdentifier;
import com.sap.sailing.racecommittee.app.utils.ManagedRaceCalculator;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ManagedRaceImpl implements ManagedRace {
    private static final long serialVersionUID = -4936566684992524001L;
    private final ManagedRaceIdentifier identifier;
    private RaceState state;
    private Map<Competitor, Boat> competitorsAndBoats;
    private CourseBase courseOnServer;
    private ManagedRaceCalculator calculator;
    private double factor;
    private Double explicitFactor;
    private final int zeroBasedIndexInFleet;

    /**
     * @param zeroBasedIndexInFleet
     *            A Series offers a sequence of RaceColumns, each of them split according to the Fleets modeled for the
     *            Series. A {@link RaceCell} describes a "slot" in this grid, defined by the series, the fleet and the
     *            race column. While this object's {@link #getName() name} represents the race column's name, this
     *            doesn't tell anything about the "horizontal" position in the "grid" or in other words what the index
     *            is of the race column in which this cell lies.
     *            <p>
     * 
     *            Indices returned by this method start with zero, meaning the first race column in the series. This
     *            corresponds to what one would get by asking {@link Util#indexOf(Iterable, Object)
     *            Util.indexOf(series.getRaceColumns(), thisCellsRaceColumn)}, except in case the first race column is a
     *            "virtual" one that holds a non-discardable carry-forward result. In this case, the second Race Column,
     *            which is the first "non-virtual" one, receives index 0.
     */
    private ManagedRaceImpl(ManagedRaceIdentifier identifier, double factor, Double explicitFactor,
            int zeroBasedIndexInFleet) {
        this.identifier = identifier;
        this.competitorsAndBoats = new HashMap<>();
        this.courseOnServer = null;
        this.factor = factor;
        this.explicitFactor = explicitFactor;
        this.zeroBasedIndexInFleet = zeroBasedIndexInFleet;
    }

    public ManagedRaceImpl(ManagedRaceIdentifier identifier, RaceState state, int zeroBasedIndexInFleet) {
        this(identifier, 0, null, zeroBasedIndexInFleet);
        this.state = state;
    }

    public ManagedRaceImpl(ManagedRaceIdentifier identifier, ManagedRaceCalculator calculator, double factor,
            Double explicitFactor, int zeroBasedIndexInFleet) {
        this(identifier, factor, explicitFactor, zeroBasedIndexInFleet);
        this.calculator = calculator;
    }

    @Override
    public RaceState getState() {
        return state;
    }

    @Override
    public String getId() {
        return identifier.getId();
    }

    @Override
    public String getName() {
        return identifier.getRaceColumnName();
    }

    @Override
    public String getRaceColumnName() {
        return getName();
    }

    @Override
    public Fleet getFleet() {
        return identifier.getFleet();
    }

    @Override
    public SeriesWithRows getSeries() {
        return identifier.getSeries();
    }

    @Override
    public RaceGroup getRaceGroup() {
        return identifier.getRaceGroup();
    }

    @Override
    public ManagedRaceIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public RaceLog getRaceLog() {
        return state == null ? null : state.getRaceLog();
    }

    @Override
    public RaceLogRaceStatus getStatus() {
        return state.getStatus();
    }

    @Override
    public CourseBase getCourseDesign() {
        return state.getCourseDesign();
    }

    @Override
    public Collection<Competitor> getCompetitors() {
        return competitorsAndBoats.keySet();
    }

    @Override
    public Map<Competitor, Boat> getCompetitorsAndBoats() {
        return competitorsAndBoats;
    }

    @Override
    public void setCompetitors(Map<Competitor, Boat> competitorsAndBoats) {
        this.competitorsAndBoats.clear();
        this.competitorsAndBoats.putAll(competitorsAndBoats);
    }

    @Override
    public CourseBase getCourseOnServer() {
        return courseOnServer;
    }

    @Override
    public void setCourseOnServer(CourseBase course) {
        courseOnServer = course;
    }

    @Override
    public boolean calculateRaceState() {
        boolean calculated = false;
        if (state == null && calculator != null) {
            state = calculator.calculateRaceState();
            calculated = true;
        }
        return calculated;
    }

    @Override
    public Result setFinishedTime(TimePoint finishedTime) {
        Result result = new Result();
        FinishingTimeFinder ftf = new FinishingTimeFinder(getRaceLog());
        if (ftf.analyze() != null) {
            if (finishedTime.after(MillisecondsTimePoint.now())) {
                result.setError(R.string.error_time_in_future);
            } else {
                if (ftf.analyze().before(finishedTime)) {
                    getState().setFinishedTime(finishedTime);
                } else {
                    result.setError(R.string.error_finished_time);
                }
            }
        }
        return result;
    }

    @Override
    public Result setFinishingTime(TimePoint finishingTime) {
        Result result = new Result();
        StartTimeFinder stf = new StartTimeFinder(new AndroidRaceLogResolver(), getRaceLog());
        if (stf.analyze() != null) {
            if (finishingTime.after(MillisecondsTimePoint.now())) {
                result.setError(R.string.error_time_in_future);
            } else {
                if (stf.analyze().getStartTime().before(finishingTime)) {
                    getState().setFinishingTime(finishingTime);
                } else {
                    result.setError(R.string.error_finishing_time);
                }
            }
        }
        return result;
    }

    public double getFactor() {
        return factor;
    }

    @Override
    public Double getExplicitFactor() {
        return explicitFactor;
    }

    @Override
    public void setExplicitFactor(Double factor) {
        this.explicitFactor = factor;
    }

    @Override
    public String toString() {
        return "ManagedRaceImpl [identifier=" + identifier + "]";
    }

    @Override
    public int getZeroBasedIndexInFleet() {
        int result = -1;
        if (zeroBasedIndexInFleet != -1) {
            // it was properly delivered by a compatible server; that's our result
            result = zeroBasedIndexInFleet;
        } else {
            // we deal with an incompatible server that doesn't know about this field yet;
            // try to compute from the surrounding race group:
            int i = 0;
            for (final RaceCell cell : getSeries().getRaceRow(getFleet()).getCells()) {
                if (cell.getName().equals(getRaceColumnName())) {
                    result = i;
                    break;
                }
                i++;
            }
            // if a cell with this race's race column name is not found, leave the index at -1
        }
        return result;
    }

    @Override
    public int getZeroBasedSeriesIndex() {
        return Util.indexOf(getRaceGroup().getSeries(), getSeries());
    }
}
