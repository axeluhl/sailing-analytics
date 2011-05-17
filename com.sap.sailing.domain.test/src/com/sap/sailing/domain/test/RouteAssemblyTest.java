package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Test;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.impl.Util;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
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
        
        TypeController routeListener = RouteData.subscribe(getEvent().getRaceList().iterator().next(),
                new ICallbackData<Route, RouteData>() {
                    private boolean first = true;
                    
                    @Override
                    public void gotData(Route route,
                            RouteData record) {
                        if (first) {
                            synchronized (semaphor) {
                                firstRoute[0] = route;
                                firstData[0] = record;
                                semaphor.notifyAll();
                            }
                            first = false;
                        }
                    }
                });
        addListenersForStoredDataAndStartController(routeListener);
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
        Course course = DomainFactory.INSTANCE.createCourse(firstRoute[0].getName(), firstData[0].getPoints());
        assertNotNull(course);
        assertEquals("Race 12 (Oro)", course.getName());
        assertEquals(9, Util.size(course.getWaypoints()));
        assertEquals(Util.size(course.getWaypoints())-1, Util.size(course.getLegs()));
    }
}
