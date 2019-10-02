package com.sap.sailing.domain.test.markpassing;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Ignore;

public class Kiel2011505MarkPassingTest extends AbstractMarkPassingTest {

    public  Kiel2011505MarkPassingTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    // The Start-Times are set very wrong on these races
    
    @Ignore
    public void testRace2() throws Exception {
        testRace("2");
    }
    @Ignore
    public void testRace3() throws Exception {
        testRace("3");
    }
    @Ignore
    public void testRace4() throws Exception {
        testRace("4");
    }

    @Override
    protected String getFileName() {
        return "event_20110609_KielerWoch-505_Race_";
    }
}

