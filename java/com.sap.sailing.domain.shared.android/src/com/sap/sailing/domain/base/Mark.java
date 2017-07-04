package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sse.common.Color;
import com.sap.sse.common.IsManagedByCache;


/**
 * A marks name is used as its ID which is only identifying the mark uniquely within a single race or course
 * definition.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface Mark extends ControlPoint, IsManagedByCache<SharedDomainFactory> {
    public Color getColor();
    public String getShape();
    public String getPattern();
    public MarkType getType();
}
