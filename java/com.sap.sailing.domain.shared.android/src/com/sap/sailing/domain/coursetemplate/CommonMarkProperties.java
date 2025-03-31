package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sse.common.Color;
import com.sap.sse.common.Named;
import com.sap.sse.common.Util;

/**
 * The properties that a {@link Mark}, a {@link MarkTemplate}, and a {@link MarkProperties}
 * object have in common.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CommonMarkProperties extends Named {
    Color getColor();
    String getShape();
    String getPattern();
    MarkType getType();
    String getShortName();

    default boolean hasEqualAppeareanceWith(CommonMarkProperties properties) {
        return Util.equalsWithNull(getColor(), properties.getColor())
                && Util.equalsWithNull(getShape(), properties.getShape())
                && Util.equalsWithNull(getPattern(), properties.getPattern())
                && Util.equalsWithNull(getType(), properties.getType())
                && Util.equalsWithNull(getShortName(), properties.getShortName());
    };
}
