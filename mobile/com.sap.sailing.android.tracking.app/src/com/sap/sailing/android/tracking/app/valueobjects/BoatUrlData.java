package com.sap.sailing.android.tracking.app.valueobjects;

import com.sap.sailing.domain.base.Boat;

public class BoatUrlData extends UrlData {

    private String mBoatId;
    private String mBoatUrl;
    private Boat mBoat;

    public BoatUrlData(String server, int port) {
        super(server, port);
    }

    public String getBoatId() {
        return mBoatId;
    }

    public void setBoatId(String boatId) {
        mBoatId = boatId;
    }

    public String getBoatUrl() {
        return mBoatUrl;
    }

    public void setBoatUrl(String boatUrl) {
        mBoatUrl = boatUrl;
    }

    public Boat getBoat() {
        return mBoat;
    }

    public void setBoat(Boat boat) {
        mBoat = boat;
    }
}
