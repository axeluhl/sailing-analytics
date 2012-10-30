package com.sap.sailing.gwt.ui.shared.racemap;


public class TwoDSegment
{	
	private double lineSlope = 0.0;
	private double lineIntercept = 0.0;

	private TwoDPoint firstPoint = null;
	private TwoDPoint secondPoint = null;
	
	public TwoDSegment(TwoDPoint p1, TwoDPoint p2)
	{
		this(p1.getX(), p1.getY(), p2.getX(), p2.getY());
	}
	
	public TwoDSegment(double x1, double y1, double x2, double y2)
	{
		if(x1 == x2)
		{
			//FIXME:
			//this.slope = Double.MAX_VALUE;
			
		}
		else
		{
			this.lineSlope = (y2 - y1) / (x2 - x1);
			this.lineIntercept = (x2 * y1 - x1 * y2) / (x2 - x1);
		}
		
		this.firstPoint = new TwoDPoint(x1, y1);
		this.secondPoint = new TwoDPoint(x2, y2);
	}

	public double getLineSlope()
	{
		return this.lineSlope;
	}

	public double getLineIntercept()
	{
		return this.lineIntercept;
	}
	
	public TwoDPoint projectionOfPointOnLine(TwoDPoint p)
	{
		double x = (this.lineSlope * p.getY() + p.getX() - this.lineSlope * this.lineIntercept ) / (this.lineSlope * this.lineSlope + 1);
		double y = this.lineSlope * x + this.lineIntercept;
		
		return new TwoDPoint(x, y);
	}

	public double distanceToLine(TwoDPoint p)
	{
		return TwoDPoint.distanceBetween(p, this.projectionOfPointOnLine(p));
	}

	public TwoDPoint getFirstPoint()
	{
		return this.firstPoint;
	}

	public TwoDPoint getSecondPoint()
	{
		return this.secondPoint;
	}
	
	@Override
	public String toString()
	{
		return "Segment[" + this.firstPoint.toString() + "|" + this.secondPoint.toString() + "]";
	}
	
	public TwoDVector asVector()
	{
		return new TwoDVector(this.firstPoint, this.secondPoint);
	}
}
