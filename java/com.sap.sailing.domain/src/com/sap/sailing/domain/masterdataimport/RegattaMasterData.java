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
    private Iterable<String> raceIds;

    public RegattaMasterData(String id, String baseName, String defaultCourseAreaId, String boatClassName,
            String scoringSchemeType, Iterable<SeriesMasterData> series, boolean isPersistent, String regattaName,
            Iterable<String> raceIds, RacingProcedureType procedureType, CourseDesignerMode designerMode) {
                this.id = id;
                this.baseName = baseName;
                this.defaultCourseAreaId = defaultCourseAreaId;
                this.defaultRacingProcedureType = procedureType;
                this.defaultCourseDesignerMode = designerMode;
                this.boatClassName = boatClassName;
                this.scoringSchemeType = scoringSchemeType;
                this.series = series;
                this.isPersistent = isPersistent;
                this.regattaName = regattaName;
                this.raceIds = raceIds;
    }

    public Iterable<String> getRaceIds() {
        return raceIds;
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
