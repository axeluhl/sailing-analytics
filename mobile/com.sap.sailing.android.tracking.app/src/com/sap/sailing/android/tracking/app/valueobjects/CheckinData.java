package com.sap.sailing.android.tracking.app.valueobjects;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.sap.sailing.android.shared.data.BaseCheckinData;
import com.sap.sailing.android.shared.data.CheckinUrlInfo;
import com.sap.sailing.android.shared.data.LeaderboardInfo;

import android.net.Uri;

public abstract class CheckinData extends BaseCheckinData {
    private String leaderboardName;
    private String leaderboardDisplayName;
    private String eventId;
    private String eventName;
    private String eventStartDateStr;
    private String eventEndDateStr;
    private String eventFirstImageUrl;
    private String eventServerUrl;
    public String checkinURL;
    public String deviceUid;
    private String uriString;
    public String checkinDigest;
    private boolean update;

    public CheckinData(UrlData data, String leaderboardDisplayName) {
        leaderboardName = Uri.decode(data.leaderboardName);
        this.leaderboardDisplayName = leaderboardDisplayName;
        deviceUid = data.deviceUuid.getStringRepresentation();
        eventId = data.eventId;
        eventName = data.eventName;
        eventStartDateStr = data.eventStartDateStr;
        eventEndDateStr = data.eventEndDateStr;
        eventFirstImageUrl = data.eventFirstImageUrl;
        eventServerUrl = data.hostWithPort;
        checkinURL = data.checkinURLStr;
        uriString = data.uriStr;
    }

    public void setCheckinDigestFromString(String checkinString)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(checkinString.getBytes("UTF-8"));
        byte[] digest = md.digest();
        StringBuilder buf = new StringBuilder();
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
        leaderboard.displayName = leaderboardDisplayName;
        leaderboard.checkinDigest = checkinDigest;
        return leaderboard;
    }

    public CheckinUrlInfo getCheckinUrl() {
        CheckinUrlInfo checkinUrlInfo = new CheckinUrlInfo();
        checkinUrlInfo.urlString = uriString;
        checkinUrlInfo.checkinDigest = checkinDigest;
        checkinUrlInfo.type = getCheckinType();
        return checkinUrlInfo;
    }

    public abstract int getCheckinType();

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }
}