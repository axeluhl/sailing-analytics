package com.sap.sailing.xrr.structureimport.buildstructure;

import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.dto.BoatClassDTO;

public class Competitors {
    private String countryCode = "";
    private String sailId = "";
    private String id = "";
    private BoatClassDTO boatClass = null;
    private String name = ""; 
    private Color color = null; 

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getSailId() {
        return sailId;
    }

    public void setSailId(String sailId) {
        this.sailId = sailId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BoatClassDTO getBoatClass() {
        return boatClass;
    }

    public void setBoatClass(BoatClassDTO boatClass) {
        this.boatClass = boatClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

}
