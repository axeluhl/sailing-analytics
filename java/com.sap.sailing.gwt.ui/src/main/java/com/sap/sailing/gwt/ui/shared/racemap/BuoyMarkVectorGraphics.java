package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.sap.sailing.domain.common.MarkType;

public class BuoyMarkVectorGraphics extends AbstractMarkVectorGraphics implements MarkVectorGraphics {

	public BuoyMarkVectorGraphics(MarkType type, String color, String shape, String pattern) {
		super(type, color, shape, pattern);
	}

	@Override
	protected void drawMarkBody(Context2d  ctx, boolean isSelected, String color) {
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
	
    private void drawBuoyWithFinishFlag(Context2d ctx, boolean isSelected, String color) {
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

    private void drawConicalBuoy(Context2d ctx, boolean isSelected, String color) {
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
}
