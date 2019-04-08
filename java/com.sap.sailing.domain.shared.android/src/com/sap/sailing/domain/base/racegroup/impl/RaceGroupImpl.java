package com.sap.sailing.domain.base.racegroup.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;
import com.sap.sse.common.impl.NamedImpl;


public class RaceGroupImpl extends NamedImpl implements RaceGroup {
    private static final long serialVersionUID = 7760879536339600827L;
    
    private final String displayName;
    private final BoatClass boatClass;
    private final CourseArea courseArea;
    private final Iterable<SeriesWithRows> series;
    private final RegattaConfiguration regattaConfiguration;
    private final boolean canBoatsOfCompetitorsChangePerRace;

    /**
     * @param series
     *            the series; for a regatta they are expected to be in the same order as {@code Regatta.getSeries()}
     *            would deliver them.
     */
    public RaceGroupImpl(String name, String displayName, BoatClass boatClass, boolean canBoatsOfCompetitorsChangePerRace, CourseArea courseArea,
            Iterable<SeriesWithRows> series, RegattaConfiguration regattaConfiguration) {
        super(name);
        this.displayName = displayName;
        this.boatClass = boatClass;
        this.canBoatsOfCompetitorsChangePerRace = canBoatsOfCompetitorsChangePerRace;
        this.courseArea = courseArea;
        this.series = series;
        this.regattaConfiguration = regattaConfiguration;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public CourseArea getDefaultCourseArea() {
        return courseArea;
    }

    @Override
    public Iterable<SeriesWithRows> getSeries() {
        return series;
    }

    @Override
    public boolean canBoatsOfCompetitorsChangePerRace() {
        return canBoatsOfCompetitorsChangePerRace;
    }

    @Override
    public BoatClass getBoatClass() {
        return boatClass;
    }

    @Override
    public RegattaConfiguration getRegattaConfiguration() {
        return regattaConfiguration;
    }

}
