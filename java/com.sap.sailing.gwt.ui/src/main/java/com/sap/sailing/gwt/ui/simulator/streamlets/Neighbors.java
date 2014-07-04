package com.sap.sailing.gwt.ui.simulator.streamlets;

public class Neighbors {
    public final int xBot;
    public final int xTop;
    public final double xMod;
    public final int yBot;
    public final int yTop;
    public final double yMod;

    public Neighbors(int xTop, int yTop, int xBot, int yBot, double xMod, double yMod) {
        this.xTop = xTop;
        this.yTop = yTop;
        this.xBot = xBot;
        this.yBot = yBot;
        this.xMod = xMod;
        this.yMod = yMod;
    }
}
