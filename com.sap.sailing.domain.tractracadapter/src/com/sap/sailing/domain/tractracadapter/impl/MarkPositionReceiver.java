package com.sap.sailing.domain.tractracadapter.impl;

import java.util.ArrayList;
import java.util.List;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.tractrac.clientmodule.ControlPoint;
import com.tractrac.clientmodule.data.ControlPointPositionData;
import com.tractrac.clientmodule.data.ICallbackData;

/**
 * The positions of the {@link ControlPoint}s of a {@link Course} are received
 * dynamically through a callback interface. Therefore, when connected to an
 * {@link Event}, and even after receiving the order of the marks for a race
 * course, these orders are not yet defined. An instance of this class
 * can be used to create the listeners needed to receive this information and
 * set it on an {@link Event}'s {@link ControlPoint}s.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class MarkPositionReceiver {
    private final TrackedEvent trackedEvent;
    private final com.tractrac.clientmodule.Event tractracEvent;
    private int received;
    
    public MarkPositionReceiver(TrackedEvent trackedEvent, com.tractrac.clientmodule.Event tractracEvent) {
        super();
        this.trackedEvent = trackedEvent;
        this.tractracEvent = tractracEvent;
    }

    /**
     * The listeners returned will, when added to a controller, receive events about the
     * position changes of marks during a race. Receiving such an event updates the Buoy's
     * {@link Track} in the {@link TrackedEvent}.
     */
    public Iterable<TypeController> getControlPointListeners() {
        List<TypeController> result = new ArrayList<TypeController>();
        TypeController controlPointListener = ControlPointPositionData.subscribe(tractracEvent,
                new ICallbackData<ControlPoint, ControlPointPositionData>() {
                    @Override
                    public void gotData(ControlPoint controlPoint, ControlPointPositionData record, boolean isLiveData) {
                        if (received++ % 1000 == 0) {
                            System.out.print("M");
                            if ((received / 1000 + 1) % 80 == 0) {
                                System.out.println();
                            }
                        }
                        Buoy buoy = DomainFactory.INSTANCE.getBuoy(controlPoint, record);
                        ((DynamicTrack<Buoy, GPSFix>) trackedEvent.getTrack(buoy)).addGPSFix(DomainFactory.INSTANCE
                                .createGPSFixMoving(record));
                    }
                }, /* fromTime */0l, /* toTime */Long.MAX_VALUE);
        result.add(controlPointListener);
        return result;
    }

}
