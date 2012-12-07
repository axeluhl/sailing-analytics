package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.MarkType;


/**
 * A marks name is used as its ID which is only identifying the mark uniquely within a single race or course
 * definition.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface Mark extends WithID, ControlPoint, IsManagedByDomainFactory {
    public String getColor();
    public String getShape();
    public String getPattern();
    public MarkType getType();
}
