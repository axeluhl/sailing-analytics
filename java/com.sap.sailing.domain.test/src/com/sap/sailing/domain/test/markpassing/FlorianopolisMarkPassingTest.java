package com.sap.sailing.domain.test.markpassing;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Test;

public class FlorianopolisMarkPassingTest extends AbstractMarkPassingTest {

    public FlorianopolisMarkPassingTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    @Test
    public void testRace1() throws IOException, InterruptedException, URISyntaxException {
        testRace(1);
    }

    @Test
    public void testRace2() throws IOException, InterruptedException, URISyntaxException {
        testRace(2);
    }

    @Test
    public void testRace3() throws IOException, InterruptedException, URISyntaxException {
        testRace(3);
    }

    @Test
    public void testRace4() throws IOException, InterruptedException, URISyntaxException {
        testRace(4);
    }

    @Override
    protected String getExpectedEventName() {
        return "ESS Florianopolis 2013";
    }
    
    @Override
    protected String getFileName(){
        return "event_20131112_ESSFlorian-Race_";
    }
}
