package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.impl.MeterDistance;

public class BoatMarkVectorGraphics extends AbstractMarkVectorGraphics {

	private final static Distance BOAT_MARK_HEIGHT_IN_METERS = new MeterDistance(6.2);
	private final static Distance BOAT_MARK_WIDTH_IN_METERS = new MeterDistance(3.5);
	private final static double BOAT_MARK_SELECTION_SCALE = 3.5;
	private final static double BOAT_MARK_SELECTION_TRANSLATE_X = -10;
	private final static double BOAT_MARK_SELECTION_TRANSLATE_Y = -30;

	public BoatMarkVectorGraphics(MarkType type, String color, String shape, String pattern) {
		super(type, color, shape, pattern);
		this.anchorPointX = BOAT_MARK_HEIGHT_IN_METERS.getMeters() / 2;
		this.anchorPointY = BOAT_MARK_WIDTH_IN_METERS.getMeters() / 2;
		this.markHeightInMeters = BOAT_MARK_HEIGHT_IN_METERS;
		this.markWidthInMeters = BOAT_MARK_WIDTH_IN_METERS;
	}
	
	protected void setUpScaleAndTranslateForMarkSelection(Context2d ctx) {
    	ctx.scale(BOAT_MARK_SELECTION_SCALE, BOAT_MARK_SELECTION_SCALE);
        ctx.translate(BOAT_MARK_SELECTION_TRANSLATE_X, BOAT_MARK_SELECTION_TRANSLATE_Y);
    }

	@Override
	protected void drawMarkBody(Context2d ctx, boolean isSelected, String color) {
		ctx.beginPath();
    	ctx.setFillStyle("#000000");
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
}
