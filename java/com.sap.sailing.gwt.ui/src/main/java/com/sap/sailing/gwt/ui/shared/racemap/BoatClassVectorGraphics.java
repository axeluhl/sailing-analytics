package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.canvas.dom.client.Context2d;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;

/**
 * The base class for boat graphics based on SVG graphics drawn to a HTML5 canvas
 * @author Frank
 *
 */
public abstract class BoatClassVectorGraphics {
    protected double boatLengthInMeters;
    protected double boatBeamInMeters;
    
    private final String mainBoatClassName;
    private final List<String> compatibleBoatClassNames;

    protected final String SAIL_FILLCOLOR = "#FFFFFF";
    protected final String SAIL_STROKECOLOR = "#000000";

    BoatClassVectorGraphics(String mainBoatClassName, double boatLengthInMeters, double boatBeamInMeters) {
        this.mainBoatClassName = mainBoatClassName;
        this.boatLengthInMeters = boatLengthInMeters;
        this.boatBeamInMeters = boatBeamInMeters;
        this.compatibleBoatClassNames = new ArrayList<String>();
    }
    
    BoatClassVectorGraphics(String mainBoatClassName, double boatLengthInMeters, double boatBeamInMeters, String...compatibleBoatClassNames) {
        this(mainBoatClassName, boatLengthInMeters, boatBeamInMeters);
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

        ctx.translate(-boatLengthInMeters * 100 / 2.0,- boatBeamInMeters * 100 / 2.0);

        drawBoat(ctx, isSelected, color);
        // drawSails(ctx, legType, tack);
        
        ctx.restore();
    }

    public double getBoatLengthInMeters() {
        return boatLengthInMeters;
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
