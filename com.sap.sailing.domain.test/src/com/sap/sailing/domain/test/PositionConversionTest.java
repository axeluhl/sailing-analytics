package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Test;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.tractrac.clientmodule.ControlPoint;
import com.tractrac.clientmodule.data.ControlPointPositionData;
import com.tractrac.clientmodule.data.ICallbackData;

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
        
        TypeController listener = ControlPointPositionData.subscribe(getEvent(),
                new ICallbackData<ControlPoint, ControlPointPositionData>() {
                    private boolean first = true;
                    
                    public void gotData(ControlPoint tracked,
                            ControlPointPositionData record) {
                        if (first) {
                            firstTracked[0] = tracked;
                            firstData[0] = record;
                            synchronized (semaphor) {
                                semaphor.notifyAll();
                            }
                            first = false;
                        }
                    }
                }, /* fromTime */0 /* means ALL */);
        addListenersAndStartController(listener);
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
