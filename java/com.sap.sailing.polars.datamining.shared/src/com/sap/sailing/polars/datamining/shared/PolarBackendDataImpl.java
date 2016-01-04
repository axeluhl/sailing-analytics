package com.sap.sailing.polars.datamining.shared;


public class PolarBackendDataImpl implements PolarBackendData {

    private static final long serialVersionUID = 2371091679485624074L;
    
    private boolean hasUpwindSpeedData;
    private double[] upwindBoatSpeedOverWindSpeed;
    
    private boolean hasDownwindSpeedData;
    private double[] downwindBoatSpeedOverWindSpeed;
    
    private boolean hasUpwindAngleData;
    private double[] upwindBoatAngleOverWindSpeed;
    
    private boolean hasDownwindAngleData;
    private double[] downwindBoatAngleOverWindSpeed;
    
    private boolean[] hasDataForAngle;
    private double[][] speedPerAnglePerWindSpeed; 
    
    public PolarBackendDataImpl() {
        //GWT
    }

    public PolarBackendDataImpl(boolean hasUpwindSpeedData, double[] upwindBoatSpeedOverWindSpeed,
            boolean hasDownwindSpeedData, double[] downwindBoatSpeedOverWindSpeed, boolean hasUpwindAngleData,
            double[] upwindBoatAngleOverWindSpeed, boolean hasDownwindAngleData,
            double[] downwindBoatAngleOverWindSpeed, boolean[] hasDataForAngle, double[][] speedPerAnglePerWindSpeed) {
        this.hasUpwindSpeedData = hasUpwindSpeedData;
        this.upwindBoatSpeedOverWindSpeed = upwindBoatSpeedOverWindSpeed;
        this.hasDownwindSpeedData = hasDownwindSpeedData;
        this.downwindBoatSpeedOverWindSpeed = downwindBoatSpeedOverWindSpeed;
        this.hasUpwindAngleData = hasUpwindAngleData;
        this.upwindBoatAngleOverWindSpeed = upwindBoatAngleOverWindSpeed;
        this.hasDownwindAngleData = hasDownwindAngleData;
        this.downwindBoatAngleOverWindSpeed = downwindBoatAngleOverWindSpeed;
        this.hasDataForAngle = hasDataForAngle;
        this.speedPerAnglePerWindSpeed = speedPerAnglePerWindSpeed;
    }

    @Override
    public boolean hasUpwindSpeedData() {
        return hasUpwindSpeedData;
    }

    @Override
    public double[] getUpwindSpeedOverWindSpeed() {
        return upwindBoatSpeedOverWindSpeed;
    }

    @Override
    public boolean hasDownwindSpeedData() {
        return hasDownwindSpeedData;
    }

    @Override
    public double[] getDownwindSpeedOverWindSpeed() {
        return downwindBoatSpeedOverWindSpeed;
    }

    @Override
    public boolean hasUpwindAngleData() {
        return hasUpwindAngleData;
    }

    @Override
    public double[] getUpwindAngleOverWindSpeed() {
        return upwindBoatAngleOverWindSpeed;
    }

    @Override
    public boolean hasDownwindAngleData() {
        return hasDownwindAngleData;
    }

    @Override
    public double[] getDownwindAngleOverWindSpeed() {
        return downwindBoatAngleOverWindSpeed;
    }

    @Override
    public double[][] getPolarDataPerWindspeedAndAngle() {
        return speedPerAnglePerWindSpeed;
    }

    @Override
    public boolean[] getDataForAngleBooleanArray() {
        return hasDataForAngle;
    }

}
