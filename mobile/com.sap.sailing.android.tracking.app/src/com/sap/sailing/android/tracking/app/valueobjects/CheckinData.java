package com.sap.sailing.android.tracking.app.valueobjects;

import com.sap.sailing.android.shared.data.LeaderboardInfo;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CheckinData {
    // public String gcmId;
    public String leaderboardName;
    public String eventId;
    public String eventName;
    public String eventStartDateStr;
    public String eventEndDateStr;
    public String eventFirstImageUrl;
    public String eventServerUrl;
    public String checkinURL;
    public String competitorName;
    public String competitorId;
    public String competitorSailId;
    public String competitorNationality;
    public String competitorCountryCode;
    public String deviceUid;
    public String uriString;
    public String checkinDigest;

    public void setCheckinDigestFromString(String checkinString) throws UnsupportedEncodingException,
            NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(checkinString.getBytes("UTF-8"));
        byte[] digest = md.digest();
        StringBuffer buf = new StringBuffer();
        for (byte byt : digest) {
            buf.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        }
        checkinDigest = buf.toString();
    }

    public EventInfo getEvent() {
        EventInfo event = new EventInfo();
        event.name = eventName;
        event.id = eventId;
        event.startMillis = Long.parseLong(eventStartDateStr);
        event.endMillis = Long.parseLong(eventEndDateStr);
        event.imageUrl = eventFirstImageUrl;
        event.server = eventServerUrl;
        event.checkinDigest = checkinDigest;
        return event;
    }

    public LeaderboardInfo getLeaderboard() {
        LeaderboardInfo leaderboard = new LeaderboardInfo();
        leaderboard.name = leaderboardName;
        leaderboard.checkinDigest = checkinDigest;
        return leaderboard;
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

    public CheckinUrlInfo getCheckinUrl(){
        CheckinUrlInfo checkinUrlInfo = new CheckinUrlInfo();
        checkinUrlInfo.urlString = uriString;
        checkinUrlInfo.checkinDigest = checkinDigest;
        return checkinUrlInfo;
    }
}