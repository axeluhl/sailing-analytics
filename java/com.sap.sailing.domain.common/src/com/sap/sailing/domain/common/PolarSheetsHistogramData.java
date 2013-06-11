package com.sap.sailing.domain.common;

import java.io.Serializable;

public interface PolarSheetsHistogramData extends Serializable{
    
    public int getAngle();
    
    public int getDataCount();
    
    public Number[] getyValues();

    public Number[] getxValues();
    
    double getCoefficiantOfVariation();
    
    public double getConfidenceMeasure();

    public void setConfidenceMeasure(double polarSheetPointConfidenceMeasure);
    

}
