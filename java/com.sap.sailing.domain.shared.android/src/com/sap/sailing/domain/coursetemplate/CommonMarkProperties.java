package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sse.common.Color;

/**
 * The properties that a {@link Mark}, a {@link MarkTemplate}, and a {@link MarkProperties}
 * object have in common.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CommonMarkProperties {
    Color getColor();
    String getShape();
    String getPattern();
    MarkType getType();
    String getShortName();
}
