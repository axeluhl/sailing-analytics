package com.sap.sailing.domain.common;

import java.io.Serializable;
import java.util.Map;

public interface PolarSheetsHistogramData extends Serializable{
    
    public int getAngle();
    
    public int getDataCount();
    
    public Number[] getyValues();

    public Number[] getxValues();
    
    double getCoefficiantOfVariation();
    
    public double getConfidenceMeasure();

    public void setConfidenceMeasure(double polarSheetPointConfidenceMeasure);
    
    public Map<String, Integer[]> getYValuesByGaugeIds();
    
    public Map<String, Integer[]> getYValuesByDay();
    
    public Map<String, Integer[]> getYValuesByDayAndGaugeId();

}
