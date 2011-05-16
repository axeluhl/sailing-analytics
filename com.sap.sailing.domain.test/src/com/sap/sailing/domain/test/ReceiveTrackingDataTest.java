package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.RaceCompetitor;
import com.tractrac.clientmodule.data.CompetitorPositionRawData;
import com.tractrac.clientmodule.data.ICallbackData;

public class ReceiveTrackingDataTest extends AbstractTracTracLiveTest {

    public ReceiveTrackingDataTest() throws URISyntaxException,
            MalformedURLException {
        super();
    }

    @Ignore
    @Test
    public void testReceiveCompetitorPosition() {
        final RaceCompetitor[] firstTracked = new RaceCompetitor[1];
        final CompetitorPositionRawData[] firstData = new CompetitorPositionRawData[1];
        final Object semaphor = new Object();
        
        ICallbackData<RaceCompetitor, CompetitorPositionRawData> positionListener = new ICallbackData<RaceCompetitor, CompetitorPositionRawData>() {
            private boolean first = true;
            
            public void gotData(RaceCompetitor tracked,
                    CompetitorPositionRawData record) {
                if (first) {
                    firstTracked[0] = tracked;
                    firstData[0] = record;
                    synchronized (semaphor) {
                        semaphor.notifyAll();
                    }
                    first = false;
                }
            }
        };
        List<TypeController> listeners = new ArrayList<TypeController>();
        for (Race race : getEvent().getRaceList()) {
            TypeController listener = CompetitorPositionRawData.subscribe(race,
                positionListener, /* fromTime */0 /* means ALL */);
            listeners.add(listener);
        }
        addListenersAndStartController(listeners.toArray(new TypeController[0]));
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
