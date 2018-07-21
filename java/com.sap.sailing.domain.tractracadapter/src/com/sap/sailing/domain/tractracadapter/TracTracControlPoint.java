package com.sap.sailing.domain.tractracadapter;

import java.util.UUID;

import com.sap.sse.common.NamedWithID;
import com.tractrac.model.lib.api.event.IEvent;
import com.tractrac.model.lib.api.route.IControlPoint;

/**
 * An interface that hides the subtle differences between a {@link IControlPoint} as obtained through TTCM and a
 * {@link com.sap.sailing.domain.tractracadapter.impl.ClientParamsPHP.ControlPoint} as obtained by reading the
 * <code>clientparams.php</code> file.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface TracTracControlPoint extends NamedWithID {
    /**
     * Refining the return type helps in looking up a control point by its UUID in {@link IEvent}.
     */
    UUID getId();
    
    String getMetadata();
    
    String getShortName();
    
    boolean getHasTwoPoints();
    
    UUID getFirstMarkId();
    
    UUID getSecondMarkId();
}
