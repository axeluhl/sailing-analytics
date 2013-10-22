package com.sap.sailing.domain.test.markpassing;

import com.sap.sailing.domain.common.TimePoint;

public class Candidate {
    private int id = 0;
    private TimePoint p;
    private double cost;
    
    public Candidate(int id, TimePoint p, double cost){
        this.id = id;
        this.p = p;
        this.cost = cost;
    }
    public int getID(){
        return id;
    }
    public TimePoint getTimePoint(){
        return p;
    }
    public double getCost(){
        return cost;
    }
}
 