package com.sap.sailing.gwt.ui.simulator.streamlets;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.sap.sailing.gwt.ui.simulator.racemap.FullCanvasOverlay;

public class Mercator {
    private final FullCanvasOverlay canvas;
    private final MapWidget map;
    private double alpha;
    private double beta;
    private double gamma;
    private double delta;

    public Mercator(FullCanvasOverlay canvas, MapWidget map) {
        this.canvas = canvas;
        this.alpha = 0.0;
        this.beta = 0.0;
        this.gamma = 0.0;
        this.delta = 0.0;
        this.map = map;
        this.calibrate();
    }

    public void calibrate() {
        Vector pointSW;
        Vector pointNE;
        int canvasHeight = canvas.getCanvas().getOffsetHeight();
        LatLng mapSW = this.map.getBounds().getSouthWest();
        LatLng mapNE = this.map.getBounds().getNorthEast();
        pointSW = this.sphere2plane(mapSW);
        pointNE = this.sphere2plane(mapNE);
        if (pointNE.x < pointSW.x) {
            pointSW.x -= 2 * Math.PI;
        }
        this.alpha = canvasHeight / (pointNE.y - pointSW.y);
        this.beta = pointSW.x;
        this.gamma = -this.alpha;
        this.delta = pointNE.y;
    }

    public Vector sphere2plane(LatLng p) {
        Vector result = new Vector();
        result.x = p.getLongitude() * Math.PI / 180.0;
        double latsin = Math.sin(p.getLatitude() * Math.PI / 180.0);
        result.y = 0.5 * Math.log((1.0 + latsin) / (1.0 - latsin));
        return result;
    }

    public LatLng plane2sphere(Point px) {
        double lng = px.getX() * 180.0 / Math.PI;
        double lat = Math.atan(Math.sinh(px.getY())) * 180.0 / Math.PI;
        return LatLng.newInstance(lat, lng);
    }

    public Vector latlng2pixel(LatLng p) {
        Vector proj = this.sphere2plane(p);
        proj.x = this.alpha * (proj.x - this.beta);
        proj.y = this.gamma * (proj.y - this.delta);
        return proj;
    }

    public LatLng pixel2latlng(Point px) {
        double p = px.getX() / this.alpha + this.beta;
        double q = px.getY() / this.gamma + this.delta;
        return this.plane2sphere(Point.newInstance(p, q));
    }

    public void clearCanvas() {
        double w = this.canvas.getCanvas().getOffsetWidth();
        double h = this.canvas.getCanvas().getOffsetHeight();
        Context2d g = this.canvas.getCanvas().getContext2d();
        g.clearRect(0, 0, w, h);
    };
}
