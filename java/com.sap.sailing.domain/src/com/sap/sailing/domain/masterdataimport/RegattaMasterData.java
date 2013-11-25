package com.sap.sailing.domain.masterdataimport;

import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;

public class RegattaMasterData {

    private final String id;
    private final String baseName;
    private final String defaultCourseAreaId;
    private final RacingProcedureType defaultRacingProcedureType;
    private final CourseDesignerMode defaultCourseDesignerMode;
    private final String boatClassName;
    private final String scoringSchemeType;
    private final Iterable<SeriesMasterData> series;
    private final boolean isPersistent;
    private final String regattaName;
    private final Iterable<String> raceIdsAsStrings;

    public RegattaMasterData(String id, String baseName, String defaultCourseAreaId, String boatClassName,
            String scoringSchemeType, Iterable<SeriesMasterData> series, boolean isPersistent, String regattaName,
            Iterable<String> raceIdsAsStrings, RacingProcedureType procedureType, CourseDesignerMode designerMode) {
        this.id = id;
        this.baseName = baseName;
        this.defaultCourseAreaId = defaultCourseAreaId;
        this.boatClassName = boatClassName;
        this.scoringSchemeType = scoringSchemeType;
        this.series = series;
        this.isPersistent = isPersistent;
        this.regattaName = regattaName;
        this.raceIdsAsStrings = raceIdsAsStrings;
        this.defaultRacingProcedureType = procedureType;
        this.defaultCourseDesignerMode = designerMode;
    }

    public Iterable<String> getRaceIdsAsStrings() {
        return raceIdsAsStrings;
    }

    public String getId() {
        return id;
    }

    public String getBaseName() {
        return baseName;
    }

    public String getDefaultCourseAreaId() {
        return defaultCourseAreaId;
    }

    public RacingProcedureType getDefaultRacingProcedureType() {
        return defaultRacingProcedureType;
    }

    public CourseDesignerMode getDefaultCourseDesignerMode() {
        return defaultCourseDesignerMode;
    }

    public String getBoatClassName() {
        return boatClassName;
    }

    public String getScoringSchemeType() {
        return scoringSchemeType;
    }

    public Iterable<SeriesMasterData> getSeries() {
        return series;
    }

    public boolean isPersistent() {
        return isPersistent;
    }

    public String getRegattaName() {
        return regattaName;
    }

}
