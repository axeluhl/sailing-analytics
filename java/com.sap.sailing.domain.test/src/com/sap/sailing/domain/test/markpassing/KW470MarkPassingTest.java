package com.sap.sailing.domain.test.markpassing;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Test;

public class KW470MarkPassingTest extends AbstractMarkPassingTest {

    public KW470MarkPassingTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    @Test
    public void testRace4() throws IOException, InterruptedException, URISyntaxException {
        testRace("4");
    }
    
    @Test
    public void testRace5() throws IOException, InterruptedException, URISyntaxException {
        testRace("5");
    }
    @Test
    public void testRace6() throws IOException, InterruptedException, URISyntaxException {
        testRace("6");
    }

    @Override
    protected String getFileName() {
        return "event_20130621_KielerWoch-470_M_gold_Race_F";
    }

    @Override
    protected String getExpectedEventName() {
        return "Kieler Woche 2013";
    }
}
