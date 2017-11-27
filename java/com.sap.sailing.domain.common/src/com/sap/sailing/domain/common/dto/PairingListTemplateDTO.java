package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

public class PairingListTemplateDTO implements Serializable {
    private static final long serialVersionUID = 7155851765154315798L;
    private int[][] pairingListTemplate;
    private double quality;
    private int flightCount = 0;
    private int groupCount = 0;
    private int competitorCount = 0;
    private int flightMultiplier = 0;
    
    public PairingListTemplateDTO() { }
    
    public PairingListTemplateDTO(int competitorCount, int flightMultiplier) {
        this(0, 0, competitorCount, flightMultiplier, null, 0.0);
    }
    
    public PairingListTemplateDTO(int competitorCount, int flightMultiplier, int[][] pairingListTemplate, double quality) {
        this(0, 0, competitorCount, flightMultiplier, pairingListTemplate, quality);
    }
    
    public PairingListTemplateDTO(int competitorCount, int[][] pairingListTemplate, double quality) {
        this(0, 0, competitorCount, 0, pairingListTemplate, quality);
    }
    
    public PairingListTemplateDTO(int flightCount, int groupCount, int competitorCount, int flightMultiplier, int[][] pairingListTemplate, double quality) {
        this.flightCount = flightCount;
        this.groupCount = groupCount;
        this.competitorCount = competitorCount;
        this.flightMultiplier = flightMultiplier;
        this.quality = quality;
        this.pairingListTemplate = pairingListTemplate;
    }
    
    public void setGroupCount(int groupCount) {
        this.groupCount = groupCount;
    }
    
    public void setFlightMultiplier(int flightMultiplier) {
        this.flightMultiplier = flightMultiplier;
    }
    
    public int[][] getPairingListTemplate() {
        return this.pairingListTemplate;
    }
    
    public double getQuality() {
        return this.quality;
    }
    
    public int getFlightCount() {
        return this.flightCount;
    }
    
    public int getGroupCount() {
        return this.groupCount;
    }
    
    public int getCompetitorCount() {
        return this.competitorCount;
    }
    
    public int getFlightMultiplier() {
        return flightMultiplier;
    }

}
