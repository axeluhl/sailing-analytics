package com.sap.sailing.dashboards.gwt.shared;

public class SinWave {
    private double period = 100; // loop every 8 calls to updateNumber
    private double scale = 5; // go between 0 and this

    private int _pos = 0;
    private double Number1 = 0;

    public SinWave(double period, double scale) {
        this.period = period;
        this.scale = scale;
    }



    public double getNexNumber() {
        _pos++;
        Number1 = (double) (Math.sin(_pos * 2 * Math.PI / period) * (scale / 2) + (scale / 2));
        return Number1;
    }
}
