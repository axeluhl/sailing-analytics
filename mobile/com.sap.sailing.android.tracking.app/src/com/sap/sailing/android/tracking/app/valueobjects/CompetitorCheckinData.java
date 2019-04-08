package com.sap.sailing.android.tracking.app.valueobjects;

import com.sap.sailing.android.shared.data.CheckinUrlInfo;

public class CompetitorCheckinData extends CheckinData {

    public String competitorName;
    public String competitorId;
    public String competitorUrl;
    public String competitorSailId;
    public String competitorNationality;
    public String competitorCountryCode;

    public CompetitorCheckinData(CompetitorUrlData urlData, String leaderboardDisplayName) {
        super(urlData, leaderboardDisplayName);
        competitorName = urlData.competitorName;
        competitorId = urlData.competitorId;
        competitorUrl = urlData.competitorUrl;
        competitorSailId = urlData.competitorSailId;
        competitorNationality = urlData.competitorNationality;
        competitorCountryCode = urlData.competitorCountryCode;
    }

    public CompetitorInfo getCompetitor() {
        CompetitorInfo competitor = new CompetitorInfo();
        competitor.name = competitorName;
        competitor.id = competitorId;
        competitor.sailId = competitorSailId;
        competitor.nationality = competitorNationality;
        competitor.countryCode = competitorCountryCode;
        competitor.checkinDigest = checkinDigest;
        return competitor;
    }

    @Override
    public int getCheckinType() {
        return CheckinUrlInfo.TYPE_COMPETITOR;
    }
}
