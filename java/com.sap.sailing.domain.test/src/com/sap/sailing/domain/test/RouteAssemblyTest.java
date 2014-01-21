package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collections;

import org.junit.Test;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.tractrac.clientmodule.Route;
import com.tractrac.clientmodule.data.ICallbackData;
import com.tractrac.clientmodule.data.RouteData;

public class RouteAssemblyTest extends AbstractTracTracLiveTest {

    public RouteAssemblyTest() throws URISyntaxException,
            MalformedURLException {
        super();
    }

    @Test
    public void testReceiveRouteData() {
        final Route[] firstRoute = new Route[1];
        final RouteData[] firstData = new RouteData[1];
        final Object semaphor = new Object();
        
        Receiver receiver = new Receiver() {
            @Override
            public void stopPreemptively() {
            }

            @Override
            public Iterable<TypeController> getTypeControllersAndStart() {
                final TypeController routeListener[] = new TypeController[1];
                routeListener[0] = RouteData.subscribe(getTracTracEvent().getRaceList().iterator().next(),
                        new ICallbackData<Route, RouteData>() {
                            private boolean first = true;

                            @Override
                            public void gotData(Route route, RouteData record, boolean isLiveData) {
                                if (first) {
                                    synchronized (semaphor) {
                                        firstRoute[0] = route;
                                        firstData[0] = record;
                                        semaphor.notifyAll();
                                        getController().remove(routeListener[0]);
                                    }
                                    first = false;
                                }
                            }
                        });
                return Collections.singleton(routeListener[0]);
            }

            @Override
            public void stopAfterProcessingQueuedEvents() {
            }

            @Override
            public void join() {
            }

            @Override
            public void join(long timeoutInMilliseconds) {
            }

            @Override
            public void stopAfterNotReceivingEventsForSomeTime(long timeoutInMilliseconds) {
            }
        };
        addListenersForStoredDataAndStartController(Collections.singleton(receiver));
        synchronized (semaphor) {
            while (firstRoute[0] == null) {
                try {
                    semaphor.wait();
                } catch (InterruptedException e) {
                    // print, ignore, wait on
                    e.printStackTrace();
                }
            }
        }
        assertNotNull(firstRoute[0]);
        assertNotNull(firstData[0]);
        Course course = DomainFactory.INSTANCE.createCourse(firstRoute[0].getName(), getTracTracControlPointsWithPassingInstructions(firstData[0].getPoints()));
        assertNotNull(course);
        assertEquals("windward-leeward training", course.getName());
        assertEquals(3, Util.size(course.getWaypoints()));
        assertEquals(Util.size(course.getWaypoints())-1, Util.size(course.getLegs()));
    }

}
