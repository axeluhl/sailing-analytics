package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.canvas.dom.client.Context2d;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;

/**
 * The base class for boat graphics based on SVG graphics drawn to a HTML5 canvas.
 * The drawing of the graphics is implemented as a list of graphics command on the Context2D,
 * We created the drawing commands not manually but used a SVG graphics with a well defined scale as a basis in combination
 * with a tool which translates this SVG graphics into the list of drawing commands. 
 * See http://wiki.sapsailing.com/wiki/boatgraphicssvg for further details.
 * 
 * @author Frank
 *
 */
public abstract class BoatClassVectorGraphics {
    protected double boatOverallLengthInMeters;
    protected double boatHullLengthInMeters;
    protected double boatBeamInMeters;
//    protected double boatCenterAlongLengthInMeters;
    
    private final String mainBoatClassName;
    private final List<String> compatibleBoatClassNames;

    protected final String SAIL_FILLCOLOR = "#555555";
    protected final String SAIL_STROKECOLOR = "#000000";

    BoatClassVectorGraphics(String mainBoatClassName, double boatOverallLengthInMeters, double boatBeamInMeters, double boatHullLengthInMeters) {
        this.mainBoatClassName = mainBoatClassName;
        this.boatOverallLengthInMeters = boatOverallLengthInMeters;
        this.boatBeamInMeters = boatBeamInMeters;
        this.boatHullLengthInMeters = boatHullLengthInMeters;
        this.compatibleBoatClassNames = new ArrayList<String>();
    }
    
    BoatClassVectorGraphics(String mainBoatClassName, double boatOverallLengthInMeters, double boatBeamInMeters, double boatHullLengthInMeters, 
            String...compatibleBoatClassNames) {
        this(mainBoatClassName, boatOverallLengthInMeters, boatBeamInMeters, boatHullLengthInMeters);
        for(String compatibleBoatClass: compatibleBoatClassNames) {
            this.compatibleBoatClassNames.add(compatibleBoatClass);
        }
    }

    protected abstract void drawBoat(Context2d ctx, boolean isSelected, String color);
    
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

    public void drawBoatToCanvas(Context2d ctx, LegType legType, Tack tack, boolean isSelected, 
            double width, double height, double angleInDegrees, double scaleFactor, String color) {

        ctx.save();

        double angleInRadians = angleInDegrees / 180.0 * Math.PI;

        ctx.clearRect(0,  0,  width, height);

        ctx.translate(width / 2.0, height / 2.0);
        ctx.rotate(angleInRadians);
        ctx.scale(scaleFactor, scaleFactor);
        ctx.translate(-boatHullLengthInMeters * 100 / 2.0,- boatBeamInMeters * 100 / 2.0);

        drawBoat(ctx, isSelected, color);
        drawSails(ctx, legType, tack);
        
        ctx.restore();
    }

    public double getBoatHullLengthInMeters() {
        return boatHullLengthInMeters;
    }
    
    public double getBoatOverallLengthInMeters() {
        return boatOverallLengthInMeters;
    }

    public double getBoatBeamInMeters() {
        return boatBeamInMeters;
    }

    public boolean isBoatClassNameCompatible(String boatClass) {
        boolean result = false;
        // remove all white space characters
        String boatClassToCheck = boatClass.replaceAll("\\s","");
        // remove all '-' characters
        boatClassToCheck = boatClass.replaceAll("-","");
        
        if(mainBoatClassName.equalsIgnoreCase(boatClassToCheck)) {
            result = true;
        } else {
            for(String compatibleName: compatibleBoatClassNames) {
                if(compatibleName.equalsIgnoreCase(boatClassToCheck)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public String getMainBoatClassName() {
        return mainBoatClassName;
    }

    public List<String> getCompatibleBoatClassNames() {
        return compatibleBoatClassNames;
    }

}
