package com.sap.sailing.polars.datamining.shared;

import java.io.Serializable;

public interface PolarBackendData extends Serializable {

    boolean hasUpwindSpeedData();

    double[] getUpwindSpeedOverWindSpeed();

    boolean hasDownwindSpeedData();

    double[] getDownwindSpeedOverWindSpeed();

    boolean hasUpwindAngleData();

    double[] getUpwindAngleOverWindSpeed();

    boolean hasDownwindAngleData();

    double[] getDownwindAngleOverWindSpeed();

    double[][] getPolarDataPerWindspeedAndAngle();

    boolean[] getDataForAngleBooleanArray();
    
}
