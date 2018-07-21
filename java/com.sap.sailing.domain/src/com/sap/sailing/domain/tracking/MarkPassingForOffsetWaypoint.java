package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.common.PassingInstruction;

/**
 * The Waypoint before an Offset Mark (see {@link PassingInstruction}) contains a MarkPassing for this mark, which does
 * not start an own leg but can be interesting for analysis.
 * 
 * @author Nicolas Klose
 * 
 */
public interface MarkPassingForOffsetWaypoint extends MarkPassing {
    
    MarkPassing getOffsetPassing();
  
}
