package com.sap.sailing.gwt.ui.simulator.streamlets;

import java.util.Date;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.maps.client.base.LatLng;
import com.sap.sailing.domain.common.Bounds;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.BoundsImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.gwt.ui.simulator.StreamletParameters;
import com.sap.sailing.gwt.ui.simulator.streamlets.PositionDTOAndDateWeigher.AverageLatitudeProvider;

public class RectField implements VectorField, AverageLatitudeProvider {

    private final Vector[][] field;

    private final double x0;
    private final double x1;
    private final double y0;
    private final double y1;

	private double motionFactor;

    private final int w;
    private final int h;

    private final double maxLength;
    private final double particleFactor;
    private final String[] colorsForSpeeds;

    public RectField(Vector[][] field, double x0, double y0, double x1, double y1, StreamletParameters parameters) {
        colorsForSpeeds = createColorsForSpeeds();
        this.x0 = x0;
        this.x1 = x1;
        this.y0 = y0;
        this.y1 = y1;
		this.motionFactor = 0.9 * parameters.motionScale;
        this.field = field;
        this.w = field.length;
        this.h = field[0].length;
        double myMaxLength = 0;
        this.particleFactor = 4.5;

        double mx = 0;
        double my = 0;
        for (int i = 0; i < this.w; i++) {
            for (int j = 0; j < this.h; j++) {
                if (field[i][j].length() > myMaxLength) {
                    mx = i;
                    my = j;
                }
                myMaxLength = Math.max(myMaxLength, field[i][j].length());
            }
        }
        this.maxLength = myMaxLength;
        mx = (mx / this.w) * (x1 - x0) + x0;
        my = (my / this.h) * (y1 - y0) + y0;
    }

    public static RectField read(String jsonData, boolean correctForSphere, StreamletParameters parameters) {
        JSONObject data = JSONParser.parseLenient(jsonData).isObject();
        int w = (int) data.get("gridWidth").isNumber().doubleValue();
        int h = (int) data.get("gridHeight").isNumber().doubleValue();
        Vector[][] field = new Vector[w][h];

        int i = 0;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Vector v = new Vector();
                v.x = data.get("field").isArray().get(i++).isNumber().doubleValue();
                v.y = data.get("field").isArray().get(i++).isNumber().doubleValue();
                if (correctForSphere) {
                    double uy = y / (h - 1);
                    double lat = data.get("y0").isNumber().doubleValue() * (1 - uy)
                            + data.get("y1").isNumber().doubleValue() * uy;
                    double m = Math.PI * lat / 180;
                    double length = v.length();
                    v.x = v.x / Math.cos(m);
                    v.setLength(length);
                }
                field[x][y] = v;
            }
        }
        RectField result = new RectField(field, data.get("x0").isNumber().doubleValue(), data.get("y0").isNumber()
                .doubleValue(), data.get("x1").isNumber().doubleValue(), data.get("y1").isNumber().doubleValue(), parameters);
        return result;
    }

    @Override
    public boolean inBounds(Position p) {
        return p.getLngDeg() >= this.x0 && p.getLngDeg() < this.x1 && p.getLatDeg() >= this.y0
                && p.getLatDeg() < this.y1;
    }

    private Vector interpolate(Position p) {
        int na = (int) Math.floor(p.getLngDeg());
        int nb = (int) Math.floor(p.getLatDeg());
        int ma = (int) Math.ceil(p.getLngDeg());
        int mb = (int) Math.ceil(p.getLatDeg());
        double fa = p.getLngDeg() - na;
        double fb = p.getLatDeg() - nb;
        Vector result = new Vector();
        result.x = this.field[na][nb].x * (1 - fa) * (1 - fb) + this.field[ma][nb].x * fa * (1 - fb)
                + this.field[na][mb].x * (1 - fa) * fb + this.field[ma][mb].x * fa * fb;
        result.y = this.field[na][nb].y * (1 - fa) * (1 - fb) + this.field[ma][nb].y * fa * (1 - fb)
                + this.field[na][mb].y * (1 - fa) * fb + this.field[ma][mb].y * fa * fb;
        return result;
    }

    @Override
    public Vector getVector(Position p, Date at) {
        double lngDeg = (this.w - 1 - 1e-6) * (p.getLngDeg() - this.x0) / (this.x1 - this.x0);
        double latDeg = (this.h - 1 - 1e-6) * (p.getLatDeg() - this.y0) / (this.y1 - this.y0);
        if ((lngDeg < 0) || (lngDeg > (this.w - 1)) || (latDeg < 0) || (latDeg > (this.h - 1))) {
            return null;
        } else {
            Position q = new DegreePosition(latDeg, lngDeg);
            return this.interpolate(q);
        }
    }

    public LatLng getCenter() {
        return LatLng.newInstance((y0 + y1) / 2.0, (x0 + x1) / 2.0);
    }

    @Override
    public double getMotionScale(int zoomLevel) {
        return this.motionFactor * Math.pow(1.7, Math.min(1.0, 6.0 - zoomLevel));
    }

    @Override
    public double getParticleWeight(Position p, Vector v) {
        return v == null ? 0 : (1.0 - v.length() / this.maxLength);
    }
    
    @Override
    public String getColor(double speed) {
        return colorsForSpeeds[getIntensity(speed)];
    }

    private String[] createColorsForSpeeds() {
        String[] colors = new String[256];
        double alpha = 1.0;
        int greyValue = 255;
        for (int i = 0; i < 256; i++) {
            colors[i] = "rgba(" + (greyValue) + "," + (greyValue) + "," + (greyValue) + "," + (alpha * i / 255.0) + ")";
        }
        return colors;
    }

    private int getIntensity(double speed) {
        double s = speed / maxLength;
        return (int) Math.min(255, 90 + Math.round(350 * s));
    }

    @Override
    public double getLineWidth(double speed) {
        return 1.0;
    }

    @Override
    public Bounds getFieldCorners() {
        // FIXME this is not date line-safe. If the field crosses +180°E (the international date line), this will fail
        Position sw = new DegreePosition(Math.min(this.y0, this.y1), Math.min(this.x0, this.x1));
        Position ne = new DegreePosition(Math.max(this.y0, this.y1), Math.max(this.x0, this.x1));
        return new BoundsImpl(sw, ne);
    }

    @Override
    public double getParticleFactor() {
        return this.particleFactor;
    }
    
    @Override
    public double getAverageLatitudeDeg() {
        return 0.0;
    }
    
    @Override
    public double getCosineOfAverageLatitude() {
        return 1.0;
    }
    
}
