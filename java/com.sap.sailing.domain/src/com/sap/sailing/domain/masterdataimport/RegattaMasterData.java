package com.sap.sailing.domain.masterdataimport;

public class RegattaMasterData {

    private String id;
    private String baseName;
    private String defaultCourseAreaId;
    private String boatClassName;
    private String scoringSchemeType;
    private Iterable<SeriesMasterData> series;
    private boolean isPersistent;
    private String regattaName;
    private Iterable<String> raceIdsAsStrings;
    

    public RegattaMasterData(String id, String baseName, String defaultCourseAreaId, String boatClassName,
            String scoringSchemeType, Iterable<SeriesMasterData> series, boolean isPersistent, String regattaName,
            Iterable<String> raceIdsAsStrings) {
                this.id = id;
                this.baseName = baseName;
                this.defaultCourseAreaId = defaultCourseAreaId;
                this.boatClassName = boatClassName;
                this.scoringSchemeType = scoringSchemeType;
                this.series = series;
                this.isPersistent = isPersistent;
                this.regattaName = regattaName;
                this.raceIdsAsStrings = raceIdsAsStrings;
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
