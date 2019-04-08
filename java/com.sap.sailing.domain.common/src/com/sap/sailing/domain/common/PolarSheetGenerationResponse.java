package com.sap.sailing.domain.common;

import java.io.Serializable;

public interface PolarSheetGenerationResponse extends Serializable{
    
    public String getId();
    
    public String getName();
    
    public PolarSheetsData getData();

}
