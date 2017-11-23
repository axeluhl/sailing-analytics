package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

public class PairingListTemplateDTO implements Serializable {
    private static final long serialVersionUID = 7155851765154315798L;
    private int[][] pairingListTemplate;
    private double quality;
    
    public PairingListTemplateDTO() { }
    
    public PairingListTemplateDTO(int[][] pairingListTemplate, double quality) {
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
