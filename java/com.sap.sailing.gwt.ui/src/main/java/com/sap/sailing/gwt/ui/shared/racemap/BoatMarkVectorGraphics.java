package com.sap.sailing.gwt.ui.shared.racemap;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.impl.MeterDistance;

public class BoatMarkVectorGraphics extends MarkVectorGraphics {

	private final static Distance BOAT_MARK_HEIGHT_IN_METERS = new MeterDistance(6.2);
	private final static Distance BOAT_MARK_WIDTH_IN_METERS = new MeterDistance(3.5);
	private final static double BOAT_MARK_SELECTION_SCALE = 3.5;
	private final static double BOAT_MARK_SELECTION_TRANSLATE_X = -10;
	private final static double BOAT_MARK_SELECTION_TRANSLATE_Y = -30;

	public BoatMarkVectorGraphics(MarkType type, String color, String shape, String pattern) {
		super(type, color, shape, pattern);
		this.anchorPointX = BOAT_MARK_HEIGHT_IN_METERS.getMeters() / 2;
		this.anchorPointY = BOAT_MARK_WIDTH_IN_METERS.getMeters() / 2;
	    this.markSelectionScaleX = BOAT_MARK_SELECTION_SCALE;
	    this.markSelectionScaleY = BOAT_MARK_SELECTION_SCALE;
	    this.markSelectionTranslateX = BOAT_MARK_SELECTION_TRANSLATE_X;
	    this.markSelectionTranslateY = BOAT_MARK_SELECTION_TRANSLATE_Y;
		this.markHeightInMeters = BOAT_MARK_HEIGHT_IN_METERS;
		this.markWidthInMeters = BOAT_MARK_WIDTH_IN_METERS;
	}
}
