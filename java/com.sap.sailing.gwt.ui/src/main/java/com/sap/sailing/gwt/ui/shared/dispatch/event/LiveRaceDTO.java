package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LiveRaceDTO implements IsSerializable {

    private String regattaName;
    private String raceName;
    private String fleetName;
    private String fleetColor;
    private Date start;

    public LiveRaceDTO() {
    }

    public String getRegattaName() {
        return regattaName;
    }

    public void setRegattaName(String regattaName) {
        this.regattaName = regattaName;
    }

    public String getRaceName() {
        return raceName;
    }

    public void setRaceName(String raceName) {
        this.raceName = raceName;
    }

    public String getFleetName() {
        return fleetName;
    }

    public void setFleetName(String fleetName) {
        this.fleetName = fleetName;
    }

    public String getFleetColor() {
        return fleetColor;
    }

    public void setFleetColor(String fleetColor) {
        this.fleetColor = fleetColor;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }
}
