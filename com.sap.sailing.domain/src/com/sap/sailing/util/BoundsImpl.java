package com.sap.sailing.util;

public class BoundsImpl extends AbstractBounds implements Bounds {
    private Point ne;
    private Point sw;
    
    public BoundsImpl(Point southWest, Point northEast) {
	ne = northEast;
	sw = southWest;
    }
    
    public Point getNE() {
	return ne;
    }

    public Point getSW() {
	return sw;
    }

    public boolean intersects(Bounds b) {
	return this.hasCornerIn(b) || b.hasCornerIn(this);
    }
    
    public boolean hasCornerIn(Bounds b) {
	// X
	return ((getSW().getX() >= b.getSW().getX() && getSW().getX() < b.getNE().getX()) ||
	        (getNE().getX() >= b.getSW().getX() && getNE().getX() < b.getNE().getX())) &&
	// Y
	       ((getNE().getY() >= b.getSW().getY() && getNE().getY() < b.getNE().getY()) ||
	        (getSW().getY() >= b.getSW().getY() && getSW().getY() < b.getNE().getY()));
    }

    public String toString() {
	return "(SW: "+getSW()+" NE: "+getNE()+")";
    }

    public Bounds extend(Point p) {
	Bounds result = this;
	if (!this.contains(p)) {
	    int swx = getSW().getX();
	    int swy = getSW().getY();
	    int nex = getNE().getX();
	    int ney = getNE().getY();
	    if (p.getX() < getSW().getX()) {
		swx = p.getX();
	    } else if (p.getX() >= getNE().getX()) {
		nex = p.getX();
	    }
	    if (p.getY() < getSW().getY()) {
		swy = p.getY();
	    } else if (p.getY() >= getNE().getY()) {
		ney = p.getY();
	    }
	    result = new BoundsImpl(new PointImpl(swx, swy), new PointImpl(nex, ney));
	}
	return result;
    }
}
