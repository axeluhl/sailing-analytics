package com.sap.sailing.gwt.ui.shared.racemap;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.gwt.ui.shared.MarkDTO;

public class MarkVectorGraphicsFactory {

	public MarkVectorGraphics getMarkVectorGraphics(MarkType markType, MarkDTO markDTO) {
		MarkVectorGraphics result;
		switch (markType) {
		case BUOY:
			result = new BuoyMarkVectorGraphics(markDTO.type, markDTO.color, markDTO.shape, markDTO.pattern);
			break;
		case STARTBOAT:
			result = new BoatMarkVectorGraphics(markDTO.type, markDTO.color, markDTO.shape, markDTO.pattern);
			break;
		case FINISHBOAT:
			result = new BoatMarkVectorGraphics(markDTO.type, markDTO.color, markDTO.shape, markDTO.pattern);
			break;
		case LANDMARK:
			result = new LandMarkVectorGraphics(markDTO.type, markDTO.color, markDTO.shape, markDTO.pattern);
			break;
		default:
			result = new BuoyMarkVectorGraphics(markDTO.type, markDTO.color, markDTO.shape, markDTO.pattern);
			break;
		}
		return result;
	}
}
