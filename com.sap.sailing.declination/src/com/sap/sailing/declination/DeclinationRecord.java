package com.sap.sailing.declination;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.Timed;

/**
 * Tells the magnetic declination for a position and a given point in time together with an anticipated annual change.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface DeclinationRecord extends Timed {
    Position getPosition();
    
    Bearing getBearing();
    
    Bearing getAnnualChange();
}
