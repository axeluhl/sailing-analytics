package com.sap.sailing.domain.masterdataimport;

import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import java.io.Serializable;
public class RegattaMasterData {

    private Serializable id;
    private String baseName;
    private String defaultCourseAreaId;
    private String boatClassName;
    private String scoringSchemeType;
    private Iterable<SeriesMasterData> series;
    private final RacingProcedureType defaultRacingProcedureType;
    private final CourseDesignerMode defaultCourseDesignerMode;
    private final RegattaConfiguration configuration;
    private boolean isPersistent;
    private String regattaName;
    private Iterable<String> raceIdsAsStrings;
    public RegattaMasterData(Serializable id, String baseName, String defaultCourseAreaId, String boatClassName,
            String scoringSchemeType, Iterable<SeriesMasterData> series, boolean isPersistent, String regattaName,
            Iterable<String> raceIdsAsStrings, RacingProcedureType procedureType, CourseDesignerMode designerMode,
            RegattaConfiguration configuration) {
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
        this.configuration = configuration;
    }

    public Iterable<String> getRaceIdsAsStrings() {
        return raceIdsAsStrings;
    }

    public Serializable getId() {
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
    
    public RegattaConfiguration getRegattaConfiguration() {
        return configuration;
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
