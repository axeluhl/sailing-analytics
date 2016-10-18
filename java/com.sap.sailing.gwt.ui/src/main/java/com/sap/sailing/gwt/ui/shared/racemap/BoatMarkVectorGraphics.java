package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.google.gwt.canvas.dom.client.Context2d;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.impl.MeterDistance;

public class BoatMarkVectorGraphics extends MarkVectorGraphics {

	private final static Distance BOAT_MARK_HEIGHT_IN_METERS = new MeterDistance(6.2);
	private final static Distance BOAT_MARK_WIDTH_IN_METERS = new MeterDistance(3.5);

	public BoatMarkVectorGraphics(MarkType type, String color, String shape, String pattern) {
		super(type, color, shape, pattern);
		this.anchorPointX = BOAT_MARK_HEIGHT_IN_METERS.getMeters() / 2;
		this.anchorPointY = BOAT_MARK_WIDTH_IN_METERS.getMeters() / 2;
		this.markHeightInMeters = BOAT_MARK_HEIGHT_IN_METERS;
		this.markWidthInMeters = BOAT_MARK_WIDTH_IN_METERS;
	}

	@Override
	protected void drawMarkSelection(Context2d ctx) {
		ctx.save();

		ctx.scale(3.5, 3.5);
		ctx.translate(50, -130);

		CanvasGradient g1 = ctx.createLinearGradient(77.8, 188, 165, 219);
		g1.addColorStop(0, "rgba(240, 240, 240, 1)");
		g1.addColorStop(1, "rgba(240, 240, 240, 0)");
		ctx.setFillStyle(g1);
		ctx.beginPath();
		ctx.moveTo(170, 181);
		ctx.translate(44.000168893718424, 180.79369636814624);
		ctx.rotate(0);
		ctx.scale(1, 1);
		ctx.arc(0, 0, 126, 0.0016373311430805408, 0.5254904360229328, false);
		ctx.scale(1, 1);
		ctx.rotate(0);
		ctx.translate(-44.000168893718424, -180.79369636814624);
		ctx.lineTo(44.2, 181);
		ctx.closePath();
		ctx.fill();
		ctx.stroke();

		CanvasGradient g2 = ctx.createLinearGradient(51, 203, 77.3, 302);
		g2.addColorStop(0, "rgba(255, 255, 255, 1)");
		g2.addColorStop(1, "rgba(255, 255, 255, 0)");
		ctx.setFillStyle(g2);
		ctx.beginPath();
		ctx.moveTo(107, 290);
		ctx.translate(43.79222318749837, 181.0010231680087);
		ctx.rotate(0);
		ctx.scale(1, 1);
		ctx.arc(0, 0, 126, 1.0452923752792398, 1.5667663411841573, false);
		ctx.scale(1, 1);
		ctx.rotate(0);
		ctx.translate(-43.79222318749837, -181.0010231680087);
		ctx.lineTo(44.2, 181);
		ctx.closePath();
		ctx.fill();
		ctx.stroke();

		CanvasGradient g3 = ctx.createLinearGradient(26.8, 197, -46.5, 269);
		g3.addColorStop(0, "rgba(255, 255, 255, 1)");
		g3.addColorStop(1, "rgba(255, 255, 255, 0)");
		ctx.setFillStyle(g3);
		ctx.beginPath();
		ctx.moveTo(-18.3, 290);
		ctx.translate(43.72636289026038, 180.32443158749652);
		ctx.rotate(0);
		ctx.scale(1, 1);
		ctx.arc(0, 0, 126, 2.0854951576078884, 2.611791628699119, false);
		ctx.scale(1, 1);
		ctx.rotate(0);
		ctx.translate(-43.72636289026038, -180.32443158749652);
		ctx.lineTo(44.2, 181);
		ctx.closePath();
		ctx.fill();
		ctx.stroke();

		CanvasGradient g4 = ctx.createLinearGradient(18.7, 172, -76.3, 140);
		g4.addColorStop(0, "rgba(255, 255, 255, 1)");
		g4.addColorStop(1, "rgba(255, 255, 255, 0)");
		ctx.setFillStyle(g4);
		ctx.beginPath();
		ctx.moveTo(-81.8, 179);
		ctx.translate(44.19920011832349, 178.55103503179944);
		ctx.rotate(0);
		ctx.scale(1, 1);
		ctx.arc(0, 0, 126, 3.1380294320163484, 3.670248678643417, false);
		ctx.scale(1, 1);
		ctx.rotate(0);
		ctx.translate(-44.19920011832349, -178.55103503179944);
		ctx.lineTo(44.2, 179);
		ctx.closePath();
		ctx.fill();
		ctx.stroke();

		CanvasGradient g5 = ctx.createLinearGradient(37.9, 147, 13.6, 58.6);
		g5.addColorStop(0, "rgba(255, 255, 255, 1)");
		g5.addColorStop(1, "rgba(255, 255, 255, 0)");
		ctx.setFillStyle(g5);
		ctx.beginPath();
		ctx.moveTo(-18.7, 71.8);
		ctx.translate(44.160311222571984, 180.99973110315514);
		ctx.rotate(0);
		ctx.scale(1, 1);
		ctx.arc(0, 0, 126, -2.0931154263055687, -1.5728622903484606, false);
		ctx.scale(1, 1);
		ctx.rotate(0);
		ctx.translate(-44.160311222571984, -180.99973110315514);
		ctx.lineTo(44.199999999999996, 181);
		ctx.closePath();
		ctx.fill();
		ctx.stroke();

		CanvasGradient g6 = ctx.createLinearGradient(80.8, 139, 134, 91.9);
		g6.addColorStop(0, "rgba(240, 240, 240, 1)");
		g6.addColorStop(1, "rgba(240, 240, 240, 0)");
		ctx.setFillStyle(g6);
		ctx.beginPath();
		ctx.moveTo(108, 72.1);
		ctx.translate(43.50304481630927, 180.3411325330301);
		ctx.rotate(0);
		ctx.scale(1, 1);
		ctx.arc(0, 0, 126, -1.0334238189446499, -0.5175711746742696, false);
		ctx.scale(1, 1);
		ctx.rotate(0);
		ctx.translate(-43.50304481630927, -180.3411325330301);
		ctx.lineTo(44.2, 181);
		ctx.closePath();
		ctx.fill();
		ctx.stroke();

		ctx.translate(-49.5, 17.2);
		ctx.restore();
	}
}
