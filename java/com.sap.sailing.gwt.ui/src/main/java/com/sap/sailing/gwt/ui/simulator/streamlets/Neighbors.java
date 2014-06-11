package com.sap.sailing.gwt.ui.simulator.streamlets;

public class Neighbors {
    public int xBot;
    public int xTop;
    public double xMod;
    public int yBot;
    public int yTop;
    public double yMod;

    public Neighbors(int xTop, int yTop, int xBot, int yBot, double xMod, double yMod) {
        this.xTop = xTop;
        this.yTop = yTop;
        this.xBot = xBot;
        this.yBot = yBot;
        this.xMod = xMod;
        this.yMod = yMod;
    }
}
