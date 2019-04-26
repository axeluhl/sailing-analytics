package com.sap.sailing.selenium.api.regatta;

import java.util.Date;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.selenium.api.core.JsonWrapper;

public class Regatta extends JsonWrapper {

    public Regatta(JSONObject json) {
        super(json);
    }

    public String getName() {
        return get("name");
    }

    public Date getStartDate() {
        return get("startDate");
    }

    public Date getEndDate() {
        return get("endDate");
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
}
