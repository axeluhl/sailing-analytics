package com.sap.sailing.gwt.ui.shared.racemap;

//FIXME: vectorul nul?

public class TwoDVector
{
	private final double re;
	private final double im;
	private final double norm;
	
	public TwoDVector(TwoDPoint startPoint, TwoDPoint endPoint)
	{
		this((endPoint.getX() - startPoint.getX()), (endPoint.getY() - startPoint.getY()));
	}
	
	public TwoDVector(double re, double im)
	{
		this.re = re;
		this.im = im;
		this.norm = Math.sqrt(this.re * this.re + this.im * this.im);
	}
	
	public TwoDVector normalize()
	{
		return new TwoDVector(this.re / this.norm, this.im / this.norm);
	}
	
	public TwoDVector add (TwoDVector vector)
	{
		return new TwoDVector(this.re + vector.getRe(), this.im + vector.getIm());
	}
	
	public TwoDVector substract(TwoDVector vector)
	{
		return new TwoDVector(this.re - vector.getRe(), this.im - vector.getIm()); 
	}
	
	public double dotProduct(TwoDVector vector)
	{
		return (this.re * vector.getRe() + this.im * vector.getIm());
	}
	
	public static double getCos(TwoDVector v1, TwoDVector v2)
	{
		return (v1.dotProduct(v2) / (v1.getNorm() * v2.getNorm()));
	}
	
	public TwoDVector multiplyScalar(double scalar)
	{
		return new TwoDVector(this.re * scalar, this.im * scalar);
	}
	
	public TwoDVector rotate(double degrees)
	{
		double temp = degrees * Math.PI / 180;
		double sin = Math.sin(temp);
		double cos = Math.cos(temp);

		return new TwoDVector((cos * this.re) + ( -1 * sin * this.im), (sin * this.re) + (cos * this.im));
	}
	
	public static TwoDPoint getRotatedPoint(TwoDPoint origin, TwoDPoint head, double degrees)
	{
		TwoDVector v = new TwoDVector(origin, head);
		v = v.rotate(degrees);

		return new TwoDPoint(v.getRe() + origin.getX(), v.getIm() + origin.getY());
	}
	
	public double getNorm()
	{
		return this.norm;
	}

	public double getRe()
	{
		return this.re;
	}

	public double getIm()
	{
		return this.im;
	}
}
