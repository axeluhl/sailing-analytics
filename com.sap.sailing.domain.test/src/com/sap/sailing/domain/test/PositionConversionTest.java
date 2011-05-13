package com.sap.sailing.domain.test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Test;

import com.tractrac.clientmodule.Event;
import com.tractrac.clientmodule.data.DataController;

public class PositionConversionTest extends AbstractTracTracLiveTest {

    public PositionConversionTest() throws URISyntaxException,
            MalformedURLException {
        super();
    }
    
    @Test
    public void testConnectivity() {
        // does nothing but test that set-up and tear-down works
    }

    @Override
    public void liveDataConnected() {
        // TODO Auto-generated method stub

    }

    @Override
    public void liveDataDisconnected() {
        // TODO Auto-generated method stub

    }

    @Override
    public void stopped() {
        // TODO Auto-generated method stub

    }

    @Override
    public void storedDataBegin() {
        // TODO Auto-generated method stub

    }

    @Override
    public void storedDataEnd() {
        // TODO Auto-generated method stub

    }

    @Override
    public void storedDataProgress(float arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void addSubscriptions(Event event, DataController controller) {
        // TODO Auto-generated method stub

    }

}
