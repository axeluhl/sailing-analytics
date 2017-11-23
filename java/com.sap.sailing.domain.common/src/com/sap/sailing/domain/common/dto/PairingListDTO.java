package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

public class PairingListDTO implements Serializable {
    private final static long serialVersionUID = 7165851765154315798L;
    private int[][] pairingListTemplate;
    private double quality;
    
    public PairingListDTO() { }
    
    public PairingListDTO(int[][] pairingListTemplate, double quality) {
        this.quality = quality;
        this.pairingListTemplate = pairingListTemplate;
    }
    
    public int[][] getPairingListTemplate() {
        return this.pairingListTemplate;
    }
    
    public double getQuality() {
        return this.quality;
    }
}
