package com.sap.sailing.domain.masterdataimport;

import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;

public class RegattaMasterData {

    private String id;
    private String baseName;
    private String defaultCourseAreaId;
    private RacingProcedureType defaultRacingProcedureType;
    private CourseDesignerMode defaultCourseDesignerMode;
    private String boatClassName;
    private String scoringSchemeType;
    private Iterable<SeriesMasterData> series;
    private boolean isPersistent;
    private String regattaName;
    private Iterable<String> raceIdsAsStrings;

    public RegattaMasterData(String id, String baseName, String defaultCourseAreaId, String boatClassName,
            String scoringSchemeType, Iterable<SeriesMasterData> series, boolean isPersistent, String regattaName,
            Iterable<String> raceIdsAsStrings, RacingProcedureType procedureType, CourseDesignerMode designerMode) {
                this.raceIdsAsStrings = raceIdsAsStrings;
                this.defaultRacingProcedureType = procedureType;
                this.defaultCourseDesignerMode = designerMode;    }

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
