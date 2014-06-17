package com.sap.sailing.domain.test.markpassing;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;

import org.junit.Test;

import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;

public class StarIDM2013MarkPassingTest extends AbstractMarkPassingTest {

    public StarIDM2013MarkPassingTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    @Test
    public void testRace5() throws IOException, InterruptedException, URISyntaxException, ParseException,
            SubscriberInitializationException, CreateModelException {
        testRace("5");
    }

    @Test
    public void testRace6() throws IOException, InterruptedException, URISyntaxException, ParseException,
            SubscriberInitializationException, CreateModelException {
        testRace("6");
    }

    @Override
    protected String getFileName() {
        return "event_20130424_IDMStarboo-Race_";
    }

    @Override
    protected String getExpectedEventName() {
        return "IDM Starboot 2013";
    }

}
