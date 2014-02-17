package com.sap.sailing.domain.test.markpassing;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Ignore;

public class Kiel2011505MarkPassingTest extends AbstractMarkPassingTest {

    public  Kiel2011505MarkPassingTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    @Ignore
    public void testRace2() throws IOException, InterruptedException, URISyntaxException {
        testRace("2");
    }
    @Ignore
    public void testRace3() throws IOException, InterruptedException, URISyntaxException {
        testRace("3");
    }
    @Ignore
    public void testRace4() throws IOException, InterruptedException, URISyntaxException {
        testRace("4");
    }

    @Override
    protected String getFileName() {
        return "event_20110609_KielerWoch-505_Race_";
    }
}

