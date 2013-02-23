package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collections;

import org.junit.Ignore;
import org.junit.Test;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.tractrac.clientmodule.ControlPoint;
import com.tractrac.clientmodule.data.ControlPointPositionData;
import com.tractrac.clientmodule.data.ICallbackData;

@Ignore
public class PositionConversionTest extends AbstractTracTracLiveTest {

    public PositionConversionTest() throws URISyntaxException,
            MalformedURLException {
        super();
    }

    @Test
    public void testConnectivity() {
        // does nothing but test that set-up and tear-down works
    }

    @Test
    public void testReceiptOfControlPointPosition() {
        final ControlPoint[] firstTracked = new ControlPoint[1];
        final ControlPointPositionData[] firstData = new ControlPointPositionData[1];
        final Object semaphor = new Object();
        
        Receiver receiver = new Receiver() {
            @Override
            public void stopPreemptively() {
            }

            @Override
            public Iterable<TypeController> getTypeControllersAndStart() {
                TypeController listener = ControlPointPositionData.subscribe(getTracTracEvent(),
                        new ICallbackData<ControlPoint, ControlPointPositionData>() {
                            private boolean first = true;

                            public void gotData(ControlPoint tracked, ControlPointPositionData record,
                                    boolean isLiveData) {
                                if (first) {
                                    synchronized (semaphor) {
                                        firstTracked[0] = tracked;
                                        firstData[0] = record;
                                        semaphor.notifyAll();
                                    }
                                    first = false;
                                }
                            }
                        }, /* fromTime */0 /* means ALL */, /* toTime */Long.MAX_VALUE);
                return Collections.singleton(listener);
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
            while (firstTracked[0] == null) {
                try {
                    semaphor.wait();
                } catch (InterruptedException e) {
                    // print, ignore, wait on
                    e.printStackTrace();
                }
            }
        }
        assertNotNull(firstTracked[0]);
        assertNotNull(firstData[0]);
        Position pos = DomainFactory.INSTANCE.createPosition(firstData[0]);
        assertNotNull(pos);
        assertEquals(firstData[0].getLatitude(), pos.getLatDeg(), /* epsilon */ 0.00000001);
        assertEquals(firstData[0].getLongitude(), pos.getLngDeg(), /* epsilon */ 0.00000001);
    }

}
