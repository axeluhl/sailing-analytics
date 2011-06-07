package com.sap.sailing.util;

public abstract class AbstractBounds implements Bounds {

    public boolean contains(Point point) {
	return (point.getX() >= getSW().getX() && point.getX() < getNE().getX() &&
		point.getY() >= getSW().getY() && point.getY() < getNE().getY());
    }
    
    public Point getNW() {
	return new PointImpl(getSW().getX(), getNE().getY());
    }

    public Point getSE() {
	return new PointImpl(getNE().getX(), getSW().getY());
    }
}
