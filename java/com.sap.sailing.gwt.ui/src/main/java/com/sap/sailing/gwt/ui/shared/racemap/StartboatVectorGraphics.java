package com.sap.sailing.gwt.ui.shared.racemap;

import com.sap.sailing.domain.common.MarkType;

public class StartboatVectorGraphics extends MarkVectorGraphics {
	
	private final static double STARTBOAT_HEIGHT_IN_METERS = 6.2;
    private final static double STARTBOAT_WIDTH_IN_METERS = 3.5;

	public StartboatVectorGraphics(MarkType type, String color, String shape, String pattern) {
		super(type, color, shape, pattern);
		this.anchorPointX = STARTBOAT_HEIGHT_IN_METERS/2;
	    this.anchorPointY = STARTBOAT_WIDTH_IN_METERS/2;
	    this.markHeightInMeters = STARTBOAT_HEIGHT_IN_METERS;
        this.markWidthInMeters = STARTBOAT_WIDTH_IN_METERS;
	}
}
