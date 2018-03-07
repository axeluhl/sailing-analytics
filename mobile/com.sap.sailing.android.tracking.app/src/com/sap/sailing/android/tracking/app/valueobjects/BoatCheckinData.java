package com.sap.sailing.android.tracking.app.valueobjects;

import com.sap.sailing.android.shared.data.CheckinUrlInfo;
import com.sap.sailing.domain.base.Boat;

public class BoatCheckinData extends CheckinData {

    private Boat mBoat;

    public BoatCheckinData(BoatUrlData boatUrlData, String leaderboardDisplayName) {
        super(boatUrlData, leaderboardDisplayName);
        mBoat = boatUrlData.getBoat();
    }

    public Boat getBoat() {
        return mBoat;
    }

    @Override
    public int getCheckinType() {
        return CheckinUrlInfo.TYPE_BOAT;
    }
}
