package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.canvas.dom.client.Context2d;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.RowingBoatClassMasterdata;
import com.sap.sse.common.Color;

/**
 * The base class for boat graphics based on SVG graphics drawn to a HTML5 canvas.
 * The drawing of the graphics is implemented as a list of graphics command on the Context2D,
 * We created the drawing commands not manually but used a SVG graphics with a well defined scale as a basis in combination
 * with a tool which translates this SVG graphics into the list of drawing commands. 
 * See http://wiki.sapsailing.com/wiki/boatgraphicssvg for further details.<p>
 * 
 * The {@link #drawBoat(Context2d, boolean, String)} implementations are expected to draw a pixel
 * size such that one pixel corresponds to one centimeter in reality. This assumption will be
 * used when scaling the boats according to hull length and zoom factor.
 * 
 * @author Frank
 *
 */
public abstract class RowingBoatClassVectorGraphics {
    protected static final String COCKPIT_FILLCOLOR = "#888888";
    protected static final String COCKPIT_STROKECOLOR = "#000000";

    /** the minimal length of the hull in pixel when the boat is drawn */
    private static final double minHullLengthInPx = 25;

    private final double hullLengthInPx;
    private final double beamInPx;
    
    private final Set<RowingBoatClassMasterdata> compatibleBoatClasses;
    
    RowingBoatClassVectorGraphics(double boatHullLengthInPx, double boatBeamInPx, 
            RowingBoatClassMasterdata... compatibleBoatClasses) {
        this.hullLengthInPx = boatHullLengthInPx;
        this.beamInPx = boatBeamInPx;
        this.compatibleBoatClasses = new HashSet<>();
        for (RowingBoatClassMasterdata compatibleBoatClass : compatibleBoatClasses) {
            this.compatibleBoatClasses.add(compatibleBoatClass);
        }
    }
    
    public Set<RowingBoatClassMasterdata> getCompatibleRowingBoatClasses() {
        return Collections.unmodifiableSet(compatibleBoatClasses);
    }

    protected abstract void drawBoat(Context2d ctx, boolean isSelected, String color);
    
    protected abstract void drawOars(Context2d ctx, boolean isPullingOars, String color);

    public void drawRowingBoatToCanvas(Context2d ctx, boolean isPullingOars, boolean isSelected, double width,
            double height, double scaleFactor, Color color) {
        ctx.save();
        ctx.clearRect(0, 0, width, height);
        ctx.translate(width / 2.0, height / 2.0);
        ctx.scale(scaleFactor, scaleFactor);
        ctx.translate(-hullLengthInPx / 2.0, -beamInPx / 2.0);
        drawBoat(ctx, isSelected, color.getAsHtml());
        drawOars(ctx, isPullingOars, color.getAsHtml());
        ctx.restore();
    }

    public double getHullLengthInPx() {
        return hullLengthInPx;
    }

    public double getBeamInPx() {
        return beamInPx;
    }

    public boolean isBoatClassNameCompatible(String boatClassName) {
        return compatibleBoatClasses.contains(BoatClassMasterdata.resolveBoatClass(boatClassName));
    }

    public double getMinHullLengthInPx() {
        return minHullLengthInPx;
    }

}
