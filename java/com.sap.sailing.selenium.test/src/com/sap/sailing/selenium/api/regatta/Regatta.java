package com.sap.sailing.selenium.api.regatta;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.selenium.api.core.JsonWrapper;
import com.sap.sse.common.TimePoint;

public class Regatta extends JsonWrapper {

    public Regatta(JSONObject json) {
        super(json);
    }

    public String getName() {
        return get("name");
    }

    public TimePoint getStartDate() {
        return getTimePointFromMilliseconds("startDate");
    }

    public TimePoint getEndDate() {
        return getTimePointFromMilliseconds("endDate");
    }

    public String getBoatClass() {
        return get("boatclass");
    }

    public String getScoringSystem() {
        return get("scoringSystem");
    }

    public String getCourseAreaId() {
        return get("courseAreaId");
    }

    public Boolean canBoatsOfCompetitorsChangePerRace() {
        return get("canBoatsOfCompetitorsChangePerRace");
    }

    public CompetitorRegistrationType getCompetitorRegistrationType() {
        return CompetitorRegistrationType.valueOf(get("competitorRegistrationType"));
    }
    
    public boolean isUseStartTimeInference() {
        return Boolean.TRUE.equals(get("useStartTimeInference"));
    }
    
    public boolean isControlTrackingFromStartAndFinishTimes() {
        return Boolean.TRUE.equals(get("controlTrackingFromStartAndFinishTimes"));
    }
}
