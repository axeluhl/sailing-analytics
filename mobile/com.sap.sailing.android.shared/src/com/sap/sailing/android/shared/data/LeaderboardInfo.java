package com.sap.sailing.android.shared.data;

public class LeaderboardInfo {

    public String name;
    /**
     * the leaderboard's display name if one has been explicitly provided, otherwise the same as {@link #name}
     */
    public String displayName;
    public String checkinDigest;
    public int rowId;
    public String serverUrl;
}
