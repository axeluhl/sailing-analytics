package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

public class PairingListDTO implements Serializable {
    private final int[][] pairingListTemplate;
    private final double quality;
    
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
