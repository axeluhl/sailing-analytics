package com.sap.sailing.util;

public class PointImpl extends AbstractPoint implements Point {
    private int x;
    private int y;
    public PointImpl(int x, int y) {
	this.x = x;
	this.y = y;
    }
    public int getX() {
	return x;
    }
    public int getY() {
	return y;
    }
    public String toString() {
	return "("+x+","+y+")";
    }
}
