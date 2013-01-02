package com.sap.sailing.domain.tractracadapter.impl;

import com.sap.sailing.domain.tractracadapter.TracTracControlPoint;

/**
 * Implements hash code and equality based on the control point's UUID.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public abstract class AbstractTracTracControlPoint implements TracTracControlPoint {

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return getId().equals(((TracTracControlPoint) obj).getId());
    }
    
}
