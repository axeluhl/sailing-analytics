package com.sap.sailing.domain.common.dto;

public class RaceColumnInSeriesDTO extends RaceColumnDTO {
    private static final long serialVersionUID = 3308901125173317674L;
    private String seriesName; 
    private String regattaName; 

    RaceColumnInSeriesDTO() {} // for GWT serialization
    
    public RaceColumnInSeriesDTO(String seriesName, String regattaName) {
        super();
        this.seriesName = seriesName;
        this.regattaName = regattaName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((regattaName == null) ? 0 : regattaName.hashCode());
        result = prime * result + ((seriesName == null) ? 0 : seriesName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        RaceColumnInSeriesDTO other = (RaceColumnInSeriesDTO) obj;
        if (regattaName == null) {
            if (other.regattaName != null)
                return false;
        } else if (!regattaName.equals(other.regattaName))
            return false;
        if (seriesName == null) {
            if (other.seriesName != null)
                return false;
        } else if (!seriesName.equals(other.seriesName))
            return false;
        return true;
    }

    @Override
    public String getSeriesName() {
        return seriesName;
    }

    public String getRegattaName() {
        return regattaName;
    }
    
 }
