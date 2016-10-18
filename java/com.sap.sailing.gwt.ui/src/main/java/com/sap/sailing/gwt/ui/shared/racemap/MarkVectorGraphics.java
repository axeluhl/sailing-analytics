package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.google.gwt.canvas.dom.client.Context2d;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.impl.MeterDistance;

/**
 * A class for course mark graphics based on SVG graphics drawn to a HTML5 canvas
 * The SVG files for the drawing can be found in the package com.sap.sailing.gwt.ui.svg.buoys
 * A general description how to convert SVG files to 'drawing commands' can be found at http://wiki.sapsailing.com/wiki/howto/development/boatgraphicssvg
 * @author Frank
 *
 */
public class MarkVectorGraphics {
    protected Distance markHeightInMeters;
    protected Distance markWidthInMeters;
    
    private final String color;
    private final String shape;
    private final String pattern;
    private final MarkType type;
    
    private static final double doublePi = 2 * Math.PI;

    protected double anchorPointX = 0.44;
    protected double anchorPointY = 1.67;
    
    private final static String DEFAULT_MARK_COLOR = "#f9ac00";
    private final static String DEFAULT_MARK_BG_COLOR = "#f0f0f0";

    public MarkVectorGraphics(MarkType type, String color, String shape, String pattern) {
        this.type = type;
        this.color = color;
        this.shape = shape;
        this.pattern = pattern;
        this.markHeightInMeters = new MeterDistance(2.1);
        this.markWidthInMeters = new MeterDistance(1.5);
    }
    
    public void drawMarkToCanvas(Context2d ctx, boolean isSelected, double width, double height, double scaleFactor) {
        ctx.save();
        ctx.clearRect(0,  0,  width, height);
        ctx.translate(width / 2.0, height / 2.0);
        ctx.scale(scaleFactor, scaleFactor);
        ctx.translate(-anchorPointX * 100,- anchorPointY * 100);
        String markColor = color != null ? color : DEFAULT_MARK_COLOR; 
        drawMark(ctx, isSelected, markColor);
        ctx.restore();
    }

	protected void drawMark(Context2d ctx, boolean isSelected, String color) {
		final MarkType markType = type == null ? MarkType.BUOY : type;
		if (isSelected) {
			drawMarkSelection(ctx);
	    }
		switch (markType) {
		case BUOY:
			if (shape != null) {
				if (Shape.CYLINDER.name().equalsIgnoreCase(shape) && pattern != null
						&& Pattern.CHECKERED.name().equalsIgnoreCase(pattern)) {
					drawBuoyWithFinishFlag(ctx, isSelected, color);
				} else if (Shape.CONICAL.name().equalsIgnoreCase(shape)) {
					drawConicalBuoy(ctx, isSelected, color);
				} else {
					drawSimpleBuoy(ctx, isSelected, color);
				}
			} else {
				drawSimpleBuoy(ctx, isSelected, color);
			}
			break;
		case FINISHBOAT:
			drawBoatmark(ctx, "#000000");
			break;
		case LANDMARK:
			drawLandmark(ctx, isSelected, color);
			break;
		case STARTBOAT:
			drawBoatmark(ctx, "#000000");
			break;
		case UMPIREBOAT:
		case CAMERABOAT:
			// umpire and camera boats are not course marks -> see
			// TrackedAssetVectorGraphics
			break;
		default:
			break;
		}
	}

    protected void drawMarkSelection(Context2d ctx) {
            ctx.save();

            ctx.scale(1.2,1.2);
            ctx.translate(-10,-30);

            CanvasGradient g1=ctx.createLinearGradient(77.8,188,165,219);
            g1.addColorStop(0,"rgba(240, 240, 240, 1)");
            g1.addColorStop(1,"rgba(240, 240, 240, 0)");
            ctx.setFillStyle(g1);
            ctx.beginPath();
            ctx.moveTo(170,181);
            ctx.translate(44.000168893718424,180.79369636814624);
            ctx.rotate(0);
            ctx.scale(1,1);
            ctx.arc(0,0,126,0.0016373311430805408,0.5254904360229328,false);
            ctx.scale(1,1);
            ctx.rotate(0);
            ctx.translate(-44.000168893718424,-180.79369636814624);
            ctx.lineTo(44.2,181);
            ctx.closePath();
            ctx.fill();
            ctx.stroke();

            CanvasGradient g2=ctx.createLinearGradient(51,203,77.3,302);
            g2.addColorStop(0,"rgba(255, 255, 255, 1)");
            g2.addColorStop(1,"rgba(255, 255, 255, 0)");
            ctx.setFillStyle(g2);
            ctx.beginPath();
            ctx.moveTo(107,290);
            ctx.translate(43.79222318749837,181.0010231680087);
            ctx.rotate(0);
            ctx.scale(1,1);
            ctx.arc(0,0,126,1.0452923752792398,1.5667663411841573,false);
            ctx.scale(1,1);
            ctx.rotate(0);
            ctx.translate(-43.79222318749837,-181.0010231680087);
            ctx.lineTo(44.2,181);
            ctx.closePath();
            ctx.fill();
            ctx.stroke();

            CanvasGradient g3=ctx.createLinearGradient(26.8,197,-46.5,269);
            g3.addColorStop(0,"rgba(255, 255, 255, 1)");
            g3.addColorStop(1,"rgba(255, 255, 255, 0)");
            ctx.setFillStyle(g3);
            ctx.beginPath();
            ctx.moveTo(-18.3,290);
            ctx.translate(43.72636289026038,180.32443158749652);
            ctx.rotate(0);
            ctx.scale(1,1);
            ctx.arc(0,0,126,2.0854951576078884,2.611791628699119,false);
            ctx.scale(1,1);
            ctx.rotate(0);
            ctx.translate(-43.72636289026038,-180.32443158749652);
            ctx.lineTo(44.2,181);
            ctx.closePath();
            ctx.fill();
            ctx.stroke();

            CanvasGradient g4=ctx.createLinearGradient(18.7,172,-76.3,140);
            g4.addColorStop(0,"rgba(255, 255, 255, 1)");
            g4.addColorStop(1,"rgba(255, 255, 255, 0)");
            ctx.setFillStyle(g4);
            ctx.beginPath();
            ctx.moveTo(-81.8,179);
            ctx.translate(44.19920011832349,178.55103503179944);
            ctx.rotate(0);
            ctx.scale(1,1);
            ctx.arc(0,0,126,3.1380294320163484,3.670248678643417,false);
            ctx.scale(1,1);
            ctx.rotate(0);
            ctx.translate(-44.19920011832349,-178.55103503179944);
            ctx.lineTo(44.2,179);
            ctx.closePath();
            ctx.fill();
            ctx.stroke();

            CanvasGradient g5=ctx.createLinearGradient(37.9,147,13.6,58.6);
            g5.addColorStop(0,"rgba(255, 255, 255, 1)");
            g5.addColorStop(1,"rgba(255, 255, 255, 0)");
            ctx.setFillStyle(g5);
            ctx.beginPath();
            ctx.moveTo(-18.7,71.8);
            ctx.translate(44.160311222571984,180.99973110315514);
            ctx.rotate(0);
            ctx.scale(1,1);
            ctx.arc(0,0,126,-2.0931154263055687,-1.5728622903484606,false);
            ctx.scale(1,1);
            ctx.rotate(0);
            ctx.translate(-44.160311222571984,-180.99973110315514);
            ctx.lineTo(44.199999999999996,181);
            ctx.closePath();
            ctx.fill();
            ctx.stroke();

            CanvasGradient g6=ctx.createLinearGradient(80.8,139,134,91.9);
            g6.addColorStop(0,"rgba(240, 240, 240, 1)");
            g6.addColorStop(1,"rgba(240, 240, 240, 0)");
            ctx.setFillStyle(g6);
            ctx.beginPath();
            ctx.moveTo(108,72.1);
            ctx.translate(43.50304481630927,180.3411325330301);
            ctx.rotate(0);
            ctx.scale(1,1);
            ctx.arc(0,0,126,-1.0334238189446499,-0.5175711746742696,false);
            ctx.scale(1,1);
            ctx.rotate(0);
            ctx.translate(-43.50304481630927,-180.3411325330301);
            ctx.lineTo(44.2,181);
            ctx.closePath();
            ctx.fill();
            ctx.stroke();

            ctx.translate(-49.5,17.2);
            ctx.restore();
    }
    
    void drawSimpleBuoy(Context2d ctx, boolean isSelected, String color) {
        ctx.setStrokeStyle("rgba(0,0,0,0)");
    
        ctx.save();
        ctx.setFillStyle(color);
        ctx.transform(12.5,0,0,12.5,-578,22.2);
        
        ctx.beginPath();
        ctx.arc(49.7,12.8,2.66,0,doublePi,false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        
        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.transform(12.5,0,0,12.5,-578,22.2);
        ctx.beginPath();
        ctx.arc(49.7,11.3,3.37,0,doublePi,false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        
        ctx.save();
        ctx.setFillStyle(color);
        ctx.transform(12.5,0,0,12.5,-578,22.2);
        ctx.beginPath();
        ctx.arc(49.6,10.6,2.66,0,doublePi,false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.transform(4.98,0,0,10.8,-205,50.4);
        ctx.beginPath();
        ctx.moveTo(50.8,9.84);
        ctx.lineTo(48.5,9.84);
        ctx.lineTo(48.5,1.44);
        ctx.lineTo(50.8,1.9);
        ctx.lineTo(50.8,9.84);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.transform(12.5,0,0,12.5,-570,22.2);
        ctx.save();
        ctx.setFillStyle(color);
        ctx.beginPath();
        ctx.moveTo(48.8,3.82);
        ctx.lineTo(48.8,-1.32);
        ctx.lineTo(57,1.05);
        ctx.lineTo(48.8,3.82);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        
        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.beginPath();
        ctx.moveTo(48.5,4.16);
        ctx.lineTo(48.5,-1.65);
        ctx.lineTo(57.8,1.03);
        ctx.lineTo(48.5,4.16);
        ctx.closePath();
        ctx.moveTo(49,-0.994);
        ctx.lineTo(49,3.47);
        ctx.lineTo(56.2,1.07);
        ctx.lineTo(49,-0.994);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        ctx.restore();
    }

    protected void drawBuoyWithFinishFlag(Context2d ctx, boolean isSelected, String color) {
        ctx.setStrokeStyle("rgba(0,0,0,0)");

        ctx.save();
        ctx.setFillStyle(color);
        ctx.transform(12.5,0,0,12.5,-578,22.2);
        ctx.beginPath();
        ctx.arc(49.7,12.8,2.66,0,doublePi,false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.transform(12.5,0,0,12.5,-578,22.2);
        ctx.beginPath();
        ctx.arc(49.7,11.3,3.37,0,doublePi,false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        
        ctx.save();
        ctx.setFillStyle(color);
        ctx.transform(12.5,0,0,12.5,-578,22.2);
        ctx.beginPath();
        ctx.arc(49.6,10.6,2.66,0,doublePi,false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        
        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.transform(4.98,0,0,10.8,-205,50.4);
        ctx.beginPath();
        ctx.moveTo(50.8,9.84);
        ctx.lineTo(48.5,9.84);
        ctx.lineTo(48.5,1.44);
        ctx.lineTo(50.8,1.9);
        ctx.lineTo(50.8,9.84);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        
        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.beginPath();
        ctx.moveTo(36.9,-0.5);
        ctx.lineTo(141.9,-0.5);
        ctx.quadraticCurveTo(141.9,-0.5,141.9,-0.5);
        ctx.lineTo(141.9,71.2);
        ctx.quadraticCurveTo(141.9,71.2,141.9,71.2);
        ctx.lineTo(36.9,71.2);
        ctx.quadraticCurveTo(36.9,71.2,36.9,71.2);
        ctx.lineTo(36.9,-0.5);
        ctx.quadraticCurveTo(36.9,-0.5,36.9,-0.5);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.setFillStyle(color);
        ctx.beginPath();
        ctx.moveTo(46,4.04);
        ctx.lineTo(76.3,4.04);
        ctx.quadraticCurveTo(76.3,4.04,76.3,4.04);
        ctx.lineTo(76.3,34.34);
        ctx.quadraticCurveTo(76.3,34.34,76.3,34.34);
        ctx.lineTo(46,34.34);
        ctx.quadraticCurveTo(46,34.34,46,34.34);
        ctx.lineTo(46,4.04);
        ctx.quadraticCurveTo(46,4.04,46,4.04);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.beginPath();
        ctx.moveTo(46.1,34.3);
        ctx.lineTo(76.4,34.3);
        ctx.quadraticCurveTo(76.4,34.3,76.4,34.3);
        ctx.lineTo(76.4,64.6);
        ctx.quadraticCurveTo(76.4,64.6,76.4,64.6);
        ctx.lineTo(46.1,64.6);
        ctx.quadraticCurveTo(46.1,64.6,46.1,64.6);
        ctx.lineTo(46.1,34.3);
        ctx.quadraticCurveTo(46.1,34.3,46.1,34.3);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.beginPath();
        ctx.moveTo(76,3.95);
        ctx.lineTo(106.3,3.95);
        ctx.quadraticCurveTo(106.3,3.95,106.3,3.95);
        ctx.lineTo(106.3,34.25);
        ctx.quadraticCurveTo(106.3,34.25,106.3,34.25);
        ctx.lineTo(76,34.25);
        ctx.quadraticCurveTo(76,34.25,76,34.25);
        ctx.lineTo(76,3.95);
        ctx.quadraticCurveTo(76,3.95,76,3.95);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        
        ctx.save();
        ctx.setFillStyle(color);
        ctx.beginPath();
        ctx.moveTo(76.1,34.2);
        ctx.lineTo(106.4,34.2);
        ctx.quadraticCurveTo(106.4,34.2,106.4,34.2);
        ctx.lineTo(106.4,64.5);
        ctx.quadraticCurveTo(106.4,64.5,106.4,64.5);
        ctx.lineTo(76.1,64.5);
        ctx.quadraticCurveTo(76.1,64.5,76.1,64.5);
        ctx.lineTo(76.1,34.2);
        ctx.quadraticCurveTo(76.1,34.2,76.1,34.2);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        
        ctx.save();
        ctx.setFillStyle(color);
        ctx.beginPath();
        ctx.moveTo(106,3.96);
        ctx.lineTo(136.3,3.96);
        ctx.quadraticCurveTo(136.3,3.96,136.3,3.96);
        ctx.lineTo(136.3,34.26);
        ctx.quadraticCurveTo(136.3,34.26,136.3,34.26);
        ctx.lineTo(106,34.26);
        ctx.quadraticCurveTo(106,34.26,106,34.26);
        ctx.lineTo(106,3.96);
        ctx.quadraticCurveTo(106,3.96,106,3.96);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        
        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.beginPath();
        ctx.moveTo(106,34.3);
        ctx.lineTo(136.3,34.3);
        ctx.quadraticCurveTo(136.3,34.3,136.3,34.3);
        ctx.lineTo(136.3,64.6);
        ctx.quadraticCurveTo(136.3,64.6,136.3,64.6);
        ctx.lineTo(106,64.6);
        ctx.quadraticCurveTo(106,64.6,106,64.6);
        ctx.lineTo(106,34.3);
        ctx.quadraticCurveTo(106,34.3,106,34.3);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        ctx.restore();
    }
    
    protected void drawLandmark(Context2d ctx, boolean isSelected, String color) {
        ctx.setStrokeStyle("rgba(0,0,0,0)");

        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.transform(4.98,0,0,10.8,-205,50.4);
        ctx.beginPath();
        ctx.moveTo(50.8,9.84);
        ctx.lineTo(48.5,9.84);
        ctx.lineTo(48.5,1.44);
        ctx.lineTo(50.8,1.9);
        ctx.lineTo(50.8,9.84);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.transform(12.5,0,0,12.5,-570,22.2);
        ctx.save();
        ctx.setFillStyle(color);
        ctx.beginPath();
        ctx.moveTo(48.8,3.82);
        ctx.lineTo(48.8,-1.32);
        ctx.lineTo(57,1.05);
        ctx.lineTo(48.8,3.82);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        
        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.beginPath();
        ctx.moveTo(48.5,4.16);
        ctx.lineTo(48.5,-1.65);
        ctx.lineTo(57.8,1.03);
        ctx.lineTo(48.5,4.16);
        ctx.closePath();
        ctx.moveTo(49,-0.994);
        ctx.lineTo(49,3.47);
        ctx.lineTo(56.2,1.07);
        ctx.lineTo(49,-0.994);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        ctx.restore();
        
        ctx.save();
        ctx.setFillStyle(color);
        ctx.beginPath();
        ctx.moveTo(83.9,209);
        ctx.bezierCurveTo(83.9,209,68.6,183,43.0,183);
        ctx.bezierCurveTo(12.3,183,7.1,209,7.1,209);
        ctx.lineTo(7.1,157);
        ctx.lineTo(83.9,157);
        ctx.lineTo(83.9,209);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        ctx.restore();
    }
    
    private void drawBoatmark(Context2d ctx, String color) {
    	ctx.beginPath();
    	ctx.setFillStyle(color);
    	ctx.setStrokeStyle("#FFFFFF");
    	ctx.setLineWidth(30.0);
    	ctx.setLineCap("butt");
    	ctx.setLineJoin("miter");
    	ctx.setMiterLimit(3.0);
    	ctx.moveTo(33,30.36);
    	ctx.quadraticCurveTo(103.47,18.96,194.65,18.55);
    	ctx.quadraticCurveTo(285.83,18.13,370.62,34.74);
    	ctx.quadraticCurveTo(455.42,51.34,520.29,87.99);
    	ctx.quadraticCurveTo(585.16,124.64,603,187.36);
    	ctx.quadraticCurveTo(584.67,244.84,520.84,277.54);
    	ctx.quadraticCurveTo(457,310.24,372.97,324.79);
    	ctx.quadraticCurveTo(288.94,339.34,197.37,339.08);
    	ctx.quadraticCurveTo(105.8,338.81,32,330.36);
    	ctx.lineTo(33,30.362205);
    	ctx.fill();
    	ctx.stroke();
       
    	ctx.setFillStyle("#FFFFFF");
    	ctx.beginPath();
    	ctx.beginPath();
    	ctx.setLineWidth(1.0);
    	ctx.setLineJoin("miter");
    	ctx.setMiterLimit(4.0);
    	ctx.moveTo(81,78);
    	ctx.lineTo(349,78);
    	ctx.lineTo(349,291);
    	ctx.lineTo(81,291);
    	ctx.lineTo(81,78);
    	ctx.fill();
    	ctx.stroke();
    	
    	ctx.setFillStyle("#000000");
    	ctx.beginPath();
    	ctx.moveTo(337,186.36);
    	ctx.quadraticCurveTo(337,180.56,334.76,175.46);
    	ctx.quadraticCurveTo(332.52,170.36,328.65,166.56);
    	ctx.quadraticCurveTo(324.78,162.76,319.59,160.56);
    	ctx.quadraticCurveTo(314.4,158.36,308.5,158.36);
    	ctx.quadraticCurveTo(302.6,158.36,297.41,160.56);
    	ctx.quadraticCurveTo(292.22,162.76,288.35,166.56);
    	ctx.quadraticCurveTo(284.48,170.36,282.24,175.46);
    	ctx.quadraticCurveTo(280,180.56,280,186.36);
    	ctx.quadraticCurveTo(280,192.16,282.24,197.26);
    	ctx.quadraticCurveTo(284.48,202.36,288.35,206.16);
    	ctx.quadraticCurveTo(292.22,209.96,297.41,212.16);
    	ctx.quadraticCurveTo(302.6,214.36,308.5,214.36);
    	ctx.quadraticCurveTo(314.4,214.36,319.59,212.16);
    	ctx.quadraticCurveTo(324.78,209.96,328.65,206.16);
    	ctx.quadraticCurveTo(332.52,202.36,334.76,197.26);
    	ctx.quadraticCurveTo(337,192.16,337,186.36);
    	ctx.stroke();
    	ctx.fill();
    	ctx.beginPath();
    }

    protected void drawConicalBuoy(Context2d ctx, boolean isSelected, String color) {
        ctx.setStrokeStyle("rgba(0,0,0,0)");

        ctx.save();
        ctx.setFillStyle(color);
        ctx.transform(12.5,0,0,12.5,-578,22.2);
        ctx.beginPath();
        ctx.arc(49.7,12.8,2.66,0,doublePi,false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.transform(12.5,0,0,12.5,-578,22.2);
        ctx.beginPath();
        ctx.arc(49.7,11.3,3.37,0,doublePi,false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        
        ctx.save();
        ctx.setFillStyle(color);
        ctx.transform(12.5,0,0,12.5,-578,22.2);
        ctx.beginPath();
        ctx.arc(49.6,10.6,2.66,0,doublePi,false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        
        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.beginPath();
        ctx.moveTo(36.9,98);
        ctx.lineTo(47.5,98);
        ctx.quadraticCurveTo(47.5,98,47.5,98);
        ctx.lineTo(47.5,154.5);
        ctx.quadraticCurveTo(47.5,154.5,47.5,154.5);
        ctx.lineTo(36.9,154.5);
        ctx.quadraticCurveTo(36.9,154.5,36.9,154.5);
        ctx.lineTo(36.9,98);
        ctx.quadraticCurveTo(36.9,98,36.9,98);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        
        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.beginPath();
        ctx.moveTo(43.6,0.372);
        ctx.lineTo(87.3,102);
        ctx.lineTo(-0.714,102);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        
        ctx.save();
        ctx.setFillStyle(color);
        ctx.beginPath();
        ctx.moveTo(43.6,14.7);
        ctx.lineTo(78.5,96.6);
        ctx.lineTo(8.07,96.6);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
    }

    protected void drawFinishBoat(Context2d ctx, boolean isSelected, String color2) {
        ctx.setStrokeStyle("rgba(0,0,0,0)");
        
        ctx.save();
        ctx.setFillStyle(color);
        ctx.transform(12.5,0,0,12.5,-578,22.2);
        
        ctx.beginPath();
        ctx.arc(49.7,12.8,2.66,0,doublePi,false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        
        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.transform(12.5,0,0,12.5,-578,22.2);
        ctx.beginPath();
        ctx.arc(49.7,11.3,3.37,0,doublePi,false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        
        ctx.save();
        ctx.setFillStyle(color);
        ctx.transform(12.5,0,0,12.5,-578,22.2);
        ctx.beginPath();
        ctx.arc(49.6,10.6,2.66,0,doublePi,false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.transform(4.98,0,0,10.8,-205,50.4);
        ctx.beginPath();
        ctx.moveTo(50.8,9.84);
        ctx.lineTo(48.5,9.84);
        ctx.lineTo(48.5,1.44);
        ctx.lineTo(50.8,1.9);
        ctx.lineTo(50.8,9.84);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.transform(12.5,0,0,12.5,-570,22.2);
        ctx.save();
        ctx.setFillStyle(color);
        ctx.beginPath();
        ctx.moveTo(48.8,3.82);
        ctx.lineTo(48.8,-1.32);
        ctx.lineTo(57,1.05);
        ctx.lineTo(48.8,3.82);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        
        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.beginPath();
        ctx.moveTo(48.5,4.16);
        ctx.lineTo(48.5,-1.65);
        ctx.lineTo(57.8,1.03);
        ctx.lineTo(48.5,4.16);
        ctx.closePath();
        ctx.moveTo(49,-0.994);
        ctx.lineTo(49,3.47);
        ctx.lineTo(56.2,1.07);
        ctx.lineTo(49,-0.994);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        ctx.restore();
    }

    public Distance getMarkHeight() {
        return markHeightInMeters;
    }

    public Distance getMarkWidth() {
        return markWidthInMeters;
    }
    
    public String getColor() {
        return color;
    }

    public String getShape() {
        return shape;
    }

    public String getPattern() {
        return pattern;
    }

    public MarkType getType() {
        return type;
    }
}
