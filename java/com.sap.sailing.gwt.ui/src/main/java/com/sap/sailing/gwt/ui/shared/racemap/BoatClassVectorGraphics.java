package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.maps.client.base.Size;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.gwt.ui.client.shared.racemap.BoatOverlay.DisplayMode;
import com.sap.sse.common.Color;

/**
 * The base class for boat graphics based on SVG graphics drawn to a HTML5 canvas.
 * The drawing of the graphics is implemented as a list of graphics command on the Context2D,
 * We created the drawing commands not manually but used a SVG graphics with a well defined scale as a basis in combination
 * with a tool which translates this SVG graphics into the list of drawing commands. 
 * See http://wiki.sapsailing.com/wiki/howto/development/boatgraphicssvg for further details.<p>
 * 
 * The {@link #drawBoat(Context2d, boolean, String)} implementations are expected to draw a pixel
 * size such that one pixel corresponds to one centimeter in reality. This assumption will be
 * used when scaling the boats according to hull length and zoom factor.
 * 
 * @author Frank
 *
 */
public abstract class BoatClassVectorGraphics {
    protected static final String SAIL_FILLCOLOR = "#888888";
    protected static final String SAIL_STROKECOLOR = "#000000";
    protected static final double BOAT_NOT_SELECTED_OPACITY = 0.3;

    /** the minimal length of the hull in pixel when the boat is drawn */
    private static final double MIN_HULL_LENGTH_IN_PX = 25;
    /** the minimal length of the beam in pixel when the boat is drawn */
    private static final double MIN_BEAM_LENGTH_IN_PX = 10;

    private final double overallLengthInPx;
    private final double hullLengthInPx;
    private final double beamInPx;
    
    private final Set<BoatClassMasterdata> compatibleBoatClasses;
    
    BoatClassVectorGraphics(double boatOverallLengthInPx, double boatBeamInPx, double boatHullLengthInPx, 
            BoatClassMasterdata... compatibleBoatClasses) {
        this.overallLengthInPx = boatOverallLengthInPx;
        this.beamInPx = boatBeamInPx;
        this.hullLengthInPx = boatHullLengthInPx;
        this.compatibleBoatClasses = new HashSet<>();
        for (BoatClassMasterdata compatibleBoatClass : compatibleBoatClasses) {
            this.compatibleBoatClasses.add(compatibleBoatClass);
        }
    }
    
    public Set<BoatClassMasterdata> getCompatibleBoatClasses() {
        return Collections.unmodifiableSet(compatibleBoatClasses);
    }

    protected abstract void drawBoat(Context2d ctx, DisplayMode displayMode, String color);
    
    protected abstract void drawDownwindPortTackSails(Context2d ctx);

    protected abstract void drawDownwindStarboardTackSails(Context2d ctx);

    protected abstract void drawUpwindPortTackSails(Context2d ctx);

    protected abstract void drawUpwindStarboardTackSails(Context2d ctx);

    protected abstract void drawReachingPortTackSails(Context2d ctx);

    protected abstract void drawReachingStarboardTackSails(Context2d ctx);

    protected abstract void drawUnknownLegTypeStarboardTackSails(Context2d ctx);

    protected abstract void drawUnknownLegTypePortTackSails(Context2d ctx);

    protected abstract void drawKillingSails(Context2d ctx);

    protected void drawSails(Context2d ctx, LegType legType, Tack tack) {
        if(tack != null && legType != null) {
            ctx.setFillStyle(SAIL_FILLCOLOR);
            ctx.setStrokeStyle(SAIL_STROKECOLOR);

            switch (tack) {
            case PORT:
                switch (legType) {
                case DOWNWIND:
                    drawDownwindPortTackSails(ctx);
                    break;
                case REACHING:
                    drawReachingPortTackSails(ctx);
                    break;
                case UPWIND:
                    drawUpwindPortTackSails(ctx);
                    break;
                }
                break;
            case STARBOARD:
                switch (legType) {
                case DOWNWIND:
                    drawDownwindStarboardTackSails(ctx);
                    break;
                case REACHING:
                    drawReachingStarboardTackSails(ctx);
                    break;
                case UPWIND:
                    drawUpwindStarboardTackSails(ctx);
                    break;
                }
                break;
            }
        }
    }

    public void drawBoatToCanvas(Context2d ctx, LegType legType, Tack tack, DisplayMode displayMode, double width,
            double height, Size scaleFactor, Color color) {
        ctx.save();
        ctx.clearRect(0, 0, width, height);
        ctx.translate(width / 2.0, height / 2.0);
        ctx.scale(scaleFactor.getWidth(), scaleFactor.getHeight());
        ctx.translate(-hullLengthInPx / 2.0, -beamInPx / 2.0);
        drawBoat(ctx, displayMode, color.getAsHtml());
        drawSails(ctx, legType, tack);
        ctx.restore();
    }

    public double getHullLengthInPx() {
        return hullLengthInPx;
    }
    
    public double getOverallLengthInPx() {
        return overallLengthInPx;
    }

    public double getBeamInPx() {
        return beamInPx;
    }

    public boolean isBoatClassNameCompatible(String boatClassName) {
        return compatibleBoatClasses.contains(BoatClassMasterdata.resolveBoatClass(boatClassName));
    }

    public double getMinHullLengthInPx() {
        return MIN_HULL_LENGTH_IN_PX;
    }

    public double getMinBeamLengthInPx() {
        return MIN_BEAM_LENGTH_IN_PX;
    }
}
