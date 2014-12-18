package com.sap.sailing.domain.base.racegroup;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sse.common.Named;

/**
 * A {@link RaceGroup} is an abstract representation of a set of races managed in fleets and series.
 * <p>
 * 
 * This representation is needed to have an abstraction layer on top of regattas and (flexible) leaderboards. It is used
 * in communication with the Android applications.
 * <p>
 * 
 * To align with the semantics of the Android application a {@link RaceGroup} consists of race rows rather than race
 * columns. A {@link RaceRow} is a set of races for one {@link Fleet}. A specific "race" of a row is called
 * {@link RaceCell}.
 * <p>
 * 
 * For example: if you have 2 fleets (blue and yellow) there will be 2 {@link RaceRow}s in your {@link RaceGroup}
 * series, each associated with {@link Fleet}. Both the rows of the blue and yellow fleet carries {@link RaceCell} named
 * "R1", "R2",...
 * <p>
 * 
 */
public interface RaceGroup extends Named {
    /**
     * Gets the {@link CourseArea} associated with the {@link RaceGroup}.
     */
    public CourseArea getDefaultCourseArea();

    /**
     * Gets the associated {@link BoatClass} if any present, otherwise
     * <code>null</code>.
     */
    public BoatClass getBoatClass();

    /**
     * Gets the collection of series.
     */
    public Iterable<SeriesWithRows> getSeries();
    
    /**
     * Gets configuration objects for {@link RacingProcedure}s of this {@link RaceGroup}'s races.
     */
    public RegattaConfiguration getRegattaConfiguration();
}
