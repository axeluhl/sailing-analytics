package com.sap.sailing.domain.base;


/**
 * A buoy's name is used as its ID which is only identifying the buoy uniquely within a single race or course
 * definition.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface Buoy extends WithID, ControlPoint, IsManagedByDomainFactory {
    public String getDisplayColor();
}
