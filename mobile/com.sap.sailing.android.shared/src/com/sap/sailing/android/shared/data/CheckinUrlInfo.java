package com.sap.sailing.android.shared.data;

public class CheckinUrlInfo {

    public static int TYPE_COMPETITOR = 0;
    public static int TYPE_MARK = 1;
    public static int TYPE_BOAT = 2;

    public String urlString;
    public String checkinDigest;
    public int type;
    public int rowId;
}
