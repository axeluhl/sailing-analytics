package com.sap.sailing.gwt.ui.simulator;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;

public class RectField {

	Point[][] field;

	private double x0;
	private double x1;
	private double y0;
	private double y1;

	private double visX0;
	private double visX1;
	private double visY0;
	private double visY1;

	private int w;
	private int h;

	private double maxLength;
	//private double numParticleFactor;

	public RectField(Point[][] field, double x0, double y0, double x1, double y1) {
		this.x0 = x0;
		this.x1 = x1;
		this.y0 = y0;
		this.y1 = y1;

		this.visX0 = x0;
		this.visY0 = y0;
		this.visX1 = x1;
		this.visY1 = y1;

		this.field = field;
		this.w = field.length;
		this.h = field[0].length;
		this.maxLength = 0;
		//this.numParticleFactor = 4.5;

		double mx = 0;
		double my = 0;
		for (int i = 0; i < this.w; i++) {
			for (int j = 0; j < this.h; j++) {
				if (length(field[i][j]) > this.maxLength) {
					mx = i;
					my = j;
				}
				this.maxLength = Math.max(this.maxLength, length(field[i][j]));
			}
		}
		mx = (mx / this.w) * (x1 - x0) + x0;
		my = (my / this.h) * (y1 - y0) + y0;
	}

	public static double length(Point p) {
		return Math.sqrt(p.getX()*p.getX() + p.getY()*p.getY());
	}

	public static RectField read(String jsonData, boolean correctForSphere) {
		JSONObject data = JSONParser.parseLenient(jsonData).isObject();
		int w = (int)data.get("gridWidth").isNumber().doubleValue();
		int h = (int)data.get("gridHeight").isNumber().doubleValue();
		Point[][] field = new Point[w][h];

		int i = 0;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				double vx = data.get("field").isArray().get(i++).isNumber().doubleValue();
				double vy = data.get("field").isArray().get(i++).isNumber().doubleValue();
				Point v = Point.newInstance(vx, vy);
				if (correctForSphere) {
					double uy = y / (h - 1);
					double lat = data.get("y0").isNumber().doubleValue() * (1 - uy) + data.get("y1").isNumber().doubleValue() * uy;
					double m = Math.PI * lat / 180;
					//double length = length(v);
					v = Point.newInstance(v.getX() / Math.cos(m), v.getY());
					//v.setLength(length);
				}
				field[x][y] = v;
			}
		}
		RectField result = new RectField(field, data.get("x0").isNumber().doubleValue(), data.get("y0").isNumber().doubleValue(), data.get("x1").isNumber().doubleValue(), data.get("y1").isNumber().doubleValue());
		return result;
	}

	public LatLng getRandomPosition() {
		double rndY = Math.random();
		double rndX = Math.random();
		double y = rndY * this.visY0 + (1 - rndY) * this.visY1;
		double x = rndX * this.visX0 + (1 - rndX) * this.visX1;
		return LatLng.newInstance(y, x);
	}

	public boolean inBounds(double x, double y) {
		return x >= this.x0 && x < this.x1 && y >= this.y0 && y < this.y1;
	}

	public Point interpolate(double a, double b) {
		int na = (int)Math.floor(a);
		int nb = (int)Math.floor(b);
		int ma = (int)Math.ceil(a);
		int mb = (int)Math.ceil(b);
		double fa = a - na;
		double fb = b - nb;

		double avgX = this.field[na][nb].getX() * (1 - fa) * (1 - fb) + this.field[ma][nb].getX() * fa * (1 - fb) + this.field[na][mb].getX() * (1 - fa) * fb + this.field[ma][mb].getX() * fa * fb;
		double avgY = this.field[na][nb].getY() * (1 - fa) * (1 - fb) + this.field[ma][nb].getY() * fa * (1 - fb) + this.field[na][mb].getY() * (1 - fa) * fb + this.field[ma][mb].getY() * fa * fb;

		return Point.newInstance(avgX, avgY);	
	};


	public Point getValue(LatLng p) {
		double a = (this.w - 1 - 1e-6) * (p.getLongitude() - this.x0) / (this.x1 - this.x0);
		double b = (this.h - 1 - 1e-6) * (p.getLatitude() - this.y0) / (this.y1 - this.y0);
		if ((a < 0)||(a > (this.w-1))||(b < 0)||(b > (this.h-1))) {
			return null;
		} else {
			return this.interpolate(a, b);
		}
	};

	public LatLng getCenter() {
		return LatLng.newInstance((y0+y1)/2.0, (x0+x1)/2.0);
	}
	
	public double getMaxLength() {
		return maxLength;
	}

	/*RectField.prototype.motionScale = function(zoomLevel) {
		return 0.9 * Math.pow(1.7, Math.min(1.0, 6.0 - zoomLevel));
	};

	RectField.prototype.particleWeight = function(p,v) {
		return 1.0 - v.length() / this.maxLength;	
	};

	RectField.prototype.getColors = function() {
		var colors = [];
		var alpha = 1.0;
		var greyValue = 255;
		for (var i = 0; i < 256; i++) {
			colors[i] = 'rgba(' + (greyValue) + ',' + (greyValue) + ',' + (greyValue) + ',' + (alpha*i/255.0) + ')';
			//this.colors[i] = 'hsla(' + 360*(0.55+0.9*(0.5-i/255)) + ',' + (100) + '% ,' + (50) + '%,' + (i/255) + ')';
		}
		return colors;
	};

	RectField.prototype.lineWidth = function(s) {
		return 1.0;
	};*/

}
