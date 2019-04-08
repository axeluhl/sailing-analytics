package com.sap.sailing.gwt.ui.shared.racemap;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.gwt.ui.shared.MarkDTO;

public class MarkVectorGraphicsFactory {
    public MarkVectorGraphics getMarkVectorGraphics(MarkDTO markDTO) {
        final MarkType markType = markDTO.type;
        final MarkVectorGraphics result;
        if (markType == null) {
            result = createBuoyMarkVectorGraphics(markDTO);
        } else {
            switch (markType) {
            case BUOY:
                result = createBuoyMarkVectorGraphics(markDTO);
                break;
            case STARTBOAT:
            case FINISHBOAT:
                result = new BoatMarkVectorGraphics(markDTO.type, markDTO.color, markDTO.shape, markDTO.pattern, markDTO.getIdAsString());
                break;
            case LANDMARK:
                result = new LandMarkVectorGraphics(markDTO.type, markDTO.color, markDTO.shape, markDTO.pattern);
                break;
            default:
                result = createBuoyMarkVectorGraphics(markDTO);
                break;
            }
        }
        return result;
    }

    private MarkVectorGraphics createBuoyMarkVectorGraphics(MarkDTO markDTO) {
        final MarkVectorGraphics result;
        if (markDTO.shape != null) {
            if (Shape.CYLINDER.name().equalsIgnoreCase(markDTO.shape) && markDTO.pattern != null
                    && Pattern.CHECKERED.name().equalsIgnoreCase(markDTO.pattern)) {
                result = new FinishFlagMarkVectorGraphics(markDTO.type, markDTO.color, markDTO.shape, markDTO.pattern);
            } else if (Shape.CONICAL.name().equalsIgnoreCase(markDTO.shape)) {
                result = new ConicalBuoyMarkVectorGraphics(markDTO.type, markDTO.color, markDTO.shape, markDTO.pattern);
            } else {
                result = new SimpleBuoyMarkVectorGraphics(markDTO.type, markDTO.color, markDTO.shape, markDTO.pattern);
            }
        } else {
            result = new SimpleBuoyMarkVectorGraphics(markDTO.type, markDTO.color, markDTO.shape, markDTO.pattern);
        }
        return result;
    }
}
