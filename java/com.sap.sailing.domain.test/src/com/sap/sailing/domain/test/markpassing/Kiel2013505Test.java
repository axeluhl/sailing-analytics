package com.sap.sailing.domain.test.markpassing;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;

import org.junit.Test;

import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;

public class Kiel2013505Test extends AbstractMarkPassingTest {

    public  Kiel2013505Test() throws MalformedURLException, URISyntaxException {
        super();
    }

    //Takes longer than 2 minutes, so the rule in AbstractTracTracLiveTest needs to be deactivated first.
    
    @Test
    public void testRace5() throws IOException, InterruptedException, URISyntaxException, ParseException, SubscriberInitializationException, CreateModelException {
        testRace("5");
    }

    @Override
    protected String getFileName() {
        return "event_20130626_KielerWoch-505_race_";
    }
    
    @Override
    protected String getExpectedEventName() {
        return "Kieler Woche 2013 - International";
    }
}


