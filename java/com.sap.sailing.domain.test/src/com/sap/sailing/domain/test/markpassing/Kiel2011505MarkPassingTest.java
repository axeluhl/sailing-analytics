package com.sap.sailing.domain.test.markpassing;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;

import org.junit.Ignore;

import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;

public class Kiel2011505MarkPassingTest extends AbstractMarkPassingTest {

    public  Kiel2011505MarkPassingTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    // The Start-Times are set very wrong on these races
    
    @Ignore
    public void testRace2() throws IOException, InterruptedException, URISyntaxException, ParseException, SubscriberInitializationException, CreateModelException {
        testRace("2");
    }
    @Ignore
    public void testRace3() throws IOException, InterruptedException, URISyntaxException, ParseException, SubscriberInitializationException, CreateModelException {
        testRace("3");
    }
    @Ignore
    public void testRace4() throws IOException, InterruptedException, URISyntaxException, ParseException, SubscriberInitializationException, CreateModelException {
        testRace("4");
    }

    @Override
    protected String getFileName() {
        return "event_20110609_KielerWoch-505_Race_";
    }
}

