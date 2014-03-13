package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.sap.sailing.domain.common.MarkType;

/**
 * A class for arbitrary tracked assets (e.g umpire boats, camera boats, etc.) based on SVG graphics drawn to a HTML5 canvas
 * @author Frank
 *
 */
public class TrackedAssetVectorGraphics {
    protected double assetHeightInMeters;
    protected double assetWidthInMeters;
    
    private final String color;
    private final MarkType type;

    private final static String DEFAULT_ASSET_COLOR = "#f9ac00";

    public TrackedAssetVectorGraphics(MarkType type, String color) {
        this.type = type;
        this.color = color;
    }
    
    public void drawMarkToCanvas(Context2d ctx, boolean isSelected, 
            double width, double height, double scaleFactor) {

        ctx.save();
        ctx.clearRect(0,  0,  width, height);

        ctx.translate(width / 2.0, height / 2.0);
        ctx.scale(scaleFactor, scaleFactor);

        ctx.translate(-assetWidthInMeters * 100 / 2.0,- assetHeightInMeters * 100 / 2.0);

        String markColor = color != null ? color : DEFAULT_ASSET_COLOR; 
        drawMark(ctx, isSelected, markColor);
        
        ctx.restore();
    }

    protected void drawMark(Context2d ctx, boolean isSelected, String color) {
        switch(type) {
            case CAMERABOAT:
                drawCameraBoat(ctx, isSelected, color);
                break;
            case UMPIREBOAT:
                drawUmpireBoat(ctx, isSelected, color);
                break;
            default:
                break;
        }
    }

    protected void drawCameraBoat(Context2d ctx, boolean isSelected, String color) {
    }

    protected void drawUmpireBoat(Context2d ctx, boolean isSelected, String color) {
    }

    public double getAssetHeightInMeters() {
        return assetHeightInMeters;
    }

    public double getAssetWidthInMeters() {
        return assetWidthInMeters;
    }
    
    public String getColor() {
        return color;
    }

    public MarkType getType() {
        return type;
    }
}
