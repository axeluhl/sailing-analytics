package com.sap.sailing.gwt.ui.simulator;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.maps.client.base.LatLng;

public class RectField {

	Vector[][] field;

	public double x0;
	public double x1;
	public double y0;
	public double y1;

	public double visX0;
	public double visX1;
	public double visY0;
	public double visY1;

	private int w;
	private int h;

	private double maxLength;
	public double particleFactor;

	public RectField(Vector[][] field, double x0, double y0, double x1, double y1) {
		this.x0 = x0;
		this.x1 = x1;
		this.y0 = y0;
		this.y1 = y1;

		this.visX0 = 0;
		this.visY0 = 0;
		this.visX1 = 0;
		this.visY1 = 0;

		this.field = field;
		this.w = field.length;
		this.h = field[0].length;
		this.maxLength = 0;
		this.particleFactor = 4.5;

		double mx = 0;
		double my = 0;
		for (int i = 0; i < this.w; i++) {
			for (int j = 0; j < this.h; j++) {
				if (field[i][j].length() > this.maxLength) {
					mx = i;
					my = j;
				}
				this.maxLength = Math.max(this.maxLength, field[i][j].length());
			}
		}
		mx = (mx / this.w) * (x1 - x0) + x0;
		my = (my / this.h) * (y1 - y0) + y0;
	}

	public static RectField read(String jsonData, boolean correctForSphere) {
		JSONObject data = JSONParser.parseLenient(jsonData).isObject();
		int w = (int)data.get("gridWidth").isNumber().doubleValue();
		int h = (int)data.get("gridHeight").isNumber().doubleValue();
		Vector[][] field = new Vector[w][h];

		int i = 0;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				Vector v = new Vector();
				v.x = data.get("field").isArray().get(i++).isNumber().doubleValue();
				v.y = data.get("field").isArray().get(i++).isNumber().doubleValue();
				if (correctForSphere) {
					double uy = y / (h - 1);
					double lat = data.get("y0").isNumber().doubleValue() * (1 - uy) + data.get("y1").isNumber().doubleValue() * uy;
					double m = Math.PI * lat / 180;
					//double length = length(v);
					v.x = v.x / Math.cos(m);
					//v.setLength(length);
				}
				field[x][y] = v;
			}
		}
		RectField result = new RectField(field, data.get("x0").isNumber().doubleValue(), data.get("y0").isNumber().doubleValue(), data.get("x1").isNumber().doubleValue(), data.get("y1").isNumber().doubleValue());
		return result;
	}

	public GeoPos getRandomPosition() {
		double rndY = Math.random();
		double rndX = Math.random();
		GeoPos result = new GeoPos();
		result.lat = rndY * this.visY0 + (1 - rndY) * this.visY1;
		result.lng = rndX * this.visX0 + (1 - rndX) * this.visX1;
		return result;
	}

	public boolean inBounds(double x, double y) {
		return x >= this.x0 && x < this.x1 && y >= this.y0 && y < this.y1;
	}

	public Vector interpolate(double a, double b) {
		int na = (int)Math.floor(a);
		int nb = (int)Math.floor(b);
		int ma = (int)Math.ceil(a);
		int mb = (int)Math.ceil(b);
		double fa = a - na;
		double fb = b - nb;

		Vector result = new Vector();
		result.x = this.field[na][nb].x * (1 - fa) * (1 - fb) + this.field[ma][nb].x * fa * (1 - fb) + this.field[na][mb].x * (1 - fa) * fb + this.field[ma][mb].x * fa * fb;
		result.y = this.field[na][nb].y * (1 - fa) * (1 - fb) + this.field[ma][nb].y * fa * (1 - fb) + this.field[na][mb].y * (1 - fa) * fb + this.field[ma][mb].y * fa * fb;

		return result;	
	}


	public Vector getVector(GeoPos p) {
		double a = (this.w - 1 - 1e-6) * (p.lng - this.x0) / (this.x1 - this.x0);
		double b = (this.h - 1 - 1e-6) * (p.lat - this.y0) / (this.y1 - this.y0);
		if ((a < 0)||(a > (this.w-1))||(b < 0)||(b > (this.h-1))) {
			return null;
		} else {
			return this.interpolate(a, b);
		}
	}

	public LatLng getCenter() {
		return LatLng.newInstance((y0+y1)/2.0, (x0+x1)/2.0);
	}
	
	public double getMaxLength() {
		return maxLength;
	}

	public double motionScale(int zoomLevel) {
		return 0.9 * Math.pow(1.7, Math.min(1.0, 6.0 - zoomLevel));
	}

	public double particleWeight(GeoPos p, Vector v) {
		return 1.0 - v.length() / this.maxLength;	
	}

	public String[] getColors() {
		String[] colors = new String[256];
		double alpha = 1.0;
		int greyValue = 255;
		for (int i = 0; i < 256; i++) {
			colors[i] = "rgba(" + (greyValue) + "," + (greyValue) + "," + (greyValue) + "," + (alpha*i/255.0) + ")";
			//this.colors[i] = 'hsla(' + 360*(0.55+0.9*(0.5-i/255)) + ',' + (100) + '% ,' + (50) + '%,' + (i/255) + ')';
		}
		return colors;
	}

	public double lineWidth(int alpha) {
		return 1.0;
	}

}
