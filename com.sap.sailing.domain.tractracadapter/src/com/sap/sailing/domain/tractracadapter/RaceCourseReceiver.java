package com.sap.sailing.domain.tractracadapter;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Event;

/**
 * The ordering of the {@link ControlPoint}s of a {@link Course} are received
 * dynamically through a callback interface. Therefore, when connected to an
 * {@link Event}, these orders are not yet defined. An instance whose class
 * implements this interface can be used to create the listeners needed to
 * receive this information and set it on an {@link Event}.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface RaceCourseReceiver {
    Iterable<TypeController> getRouteListeners();
}
