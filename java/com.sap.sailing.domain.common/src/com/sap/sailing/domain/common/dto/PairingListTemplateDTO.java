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
    private double boatAssignmentQualitiy;
    private Iterable<String> selectedFlightNames;
    
    public PairingListTemplateDTO() { }
    
    public PairingListTemplateDTO(int competitorCount, int flightMultiplier, int boatChangeFactor) {
        this(0, 0, competitorCount, flightMultiplier, boatChangeFactor, 0 , null, 0.0, 0.0, /* selected flight names */ null);
    }
    
    public PairingListTemplateDTO(int competitorCount, int flightMultiplier, int[][] pairingListTemplate, double quality) {
        this(0, 0, competitorCount, flightMultiplier, 0, 0, pairingListTemplate, quality, 0.0, /* selected flight names */ null);
    }
    
    public PairingListTemplateDTO(int competitorCount, int[][] pairingListTemplate, double quality) {
        this(0, 0, competitorCount, 0, 0, 0, pairingListTemplate, quality, 0.0, /* selected flight names */ null);
    }
    
    public PairingListTemplateDTO(int flightCount, int groupCount, int competitorCount, int flightMultiplier, int boatChangeFactor, int[][] pairingListTemplate, double quality) {
        this(flightCount, groupCount, competitorCount, flightMultiplier, boatChangeFactor, 0, pairingListTemplate, quality, 0.0, /* selected flight names */ null);
    }
    
    public PairingListTemplateDTO(int flightCount, int groupCount, int competitorCount, int flightMultiplier, int boatChangeFactor, int boatChanges, int[][] pairingListTemplate, double quality, double boatassignmentqualitiy) {
        this(flightCount, groupCount, competitorCount, flightMultiplier, boatChangeFactor, boatChanges, pairingListTemplate, quality, boatassignmentqualitiy, /* selected flight names */ null);
    }
    
    public PairingListTemplateDTO(int flightCount, int groupCount, int competitorCount, int flightMultiplier, int boatChangeFactor,
            int boatChanges, int[][] pairingListTemplate, double quality, double boatAssigmentQualitiy, Iterable<String> selectedFlightNames) {
        this.flightCount = flightCount;
        this.groupCount = groupCount;
        this.competitorCount = competitorCount;
        this.flightMultiplier = flightMultiplier;
        this.boatChangeFactor = boatChangeFactor;
        this.boatChanges = boatChanges;
        this.boatAssignmentQualitiy = boatAssigmentQualitiy;
        this.quality = quality;
        this.pairingListTemplate = pairingListTemplate;
        this.selectedFlightNames = selectedFlightNames;
    }
    
    public void setFlightCount(int flightCount){
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
    	return this.boatAssignmentQualitiy;
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

}
