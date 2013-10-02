package com.sap.sailing.domain.common;

import java.io.Serializable;

public interface PolarSheetGenerationResponse extends Serializable{
    
    public String getId();
    
    public String getBoatClassName();
    
    public PolarSheetsData getData();

}
