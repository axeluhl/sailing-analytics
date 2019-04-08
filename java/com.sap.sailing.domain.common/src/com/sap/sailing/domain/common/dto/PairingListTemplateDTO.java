package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

public class PairingListTemplateDTO implements Serializable {
    
    private static final long serialVersionUID = 7155851765154315798L;
    
    private int flightCount = 0;
    private int groupCount = 0;
    private int competitorCount = 0;
    private int flightMultiplier = 0;
    private int boatChangeFactor = 0;
    private int boatChanges;
    private int[][] pairingListTemplate;
    private double quality;
    private double boatAssignmentQuality;
    private Iterable<String> selectedFlightNames;
    
    public PairingListTemplateDTO() { }
    
    public PairingListTemplateDTO(int competitorCount, int flightMultiplier, int boatChangeFactor) {
        this(0, 0, competitorCount, flightMultiplier, boatChangeFactor, 0 , null, 0.0, 0.0, /* selected flight names */ null);
    }
    
    public PairingListTemplateDTO(int competitorCount, int flightMultiplier, int[][] pairingListTemplate, double quality) {
        this(/* flight count */ 0, /* group count */ 0, competitorCount, flightMultiplier, /* boat change factor */ 0, /* boat changes */ 0, pairingListTemplate, quality, /* boat assignment quality */ 0.0, /* selected flight names */ null);
    }
    
    public PairingListTemplateDTO(int competitorCount, int[][] pairingListTemplate, double quality) {
        this(/* flight count */ 0, /* group count */ 0, competitorCount, /* flight multiplier */ 1, /* boat change factor */ 0, /* boat changes */ 0, pairingListTemplate, quality, /* boat assignment quality */ 0.0, /* selected flight names */ null);
    }
    
    public PairingListTemplateDTO(int flightCount, int groupCount, int competitorCount, int flightMultiplier, int boatChangeFactor, int[][] pairingListTemplate, double quality) {
        this(flightCount, groupCount, competitorCount, flightMultiplier, boatChangeFactor, /* boat Changes */ 0, pairingListTemplate, quality, /* boat assignment quality */ 0.0, /* selected flight names */ null);
    }
    
    public PairingListTemplateDTO(int flightCount, int groupCount, int competitorCount, int flightMultiplier, int boatChangeFactor, int boatChanges, int[][] pairingListTemplate, double quality, double boatAssignmentQuality) {
        this(flightCount, groupCount, competitorCount, flightMultiplier, boatChangeFactor, boatChanges, pairingListTemplate, quality, boatAssignmentQuality, /* selected flight names */ null);
    }
    
    public PairingListTemplateDTO(int flightCount, int groupCount, int competitorCount, int flightMultiplier, int boatChangeFactor,
            int boatChanges, int[][] pairingListTemplate, double quality, double boatAssigmentQuality, Iterable<String> selectedFlightNames) {
        this.flightCount = flightCount;
        this.groupCount = groupCount;
        this.competitorCount = competitorCount;
        this.flightMultiplier = flightMultiplier;
        this.boatChangeFactor = boatChangeFactor;
        this.boatChanges = boatChanges;
        this.boatAssignmentQuality = boatAssigmentQuality;
        this.quality = quality;
        this.pairingListTemplate = pairingListTemplate;
        this.selectedFlightNames = selectedFlightNames;
    }
    
    public void setFlightCount(int flightCount) {
        this.flightCount=flightCount;
    }
    
    public void setGroupCount(int groupCount) {
        this.groupCount = groupCount;
    }
    
    public void setFlightMultiplier(int flightMultiplier) {
        this.flightMultiplier = flightMultiplier;
    }
    
    public void setTolerance(int tolerance) {
        this.boatChangeFactor = tolerance;
    }
    
    public void setSelectedFlightNames(Iterable<String> selectedFlightNames) {
        this.selectedFlightNames = selectedFlightNames;
    }
    
    public int[][] getPairingListTemplate() {
        return this.pairingListTemplate;
    }
    
    public double getQuality() {
        return this.quality;
    }
    
    public double getBoatAssignmentQuality(){
    	return this.boatAssignmentQuality;
    }
    
    public int getBoatChanges(){
    	return this.boatChanges;
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
    
    public int getBoatChangeFactor() {
        return boatChangeFactor;
    }
    
    public Iterable<String> getSelectedFlightNames() {
        return selectedFlightNames;
    }
    
    public void swapColumns(final int indexA, final int indexB) {
        for(int row = 0; row < this.pairingListTemplate.length; row++) {
            int tmpField = this.pairingListTemplate[row][indexA];
            this.pairingListTemplate[row][indexA] = this.pairingListTemplate[row][indexB];
            this.pairingListTemplate[row][indexB] = tmpField;
        }
    }

}
