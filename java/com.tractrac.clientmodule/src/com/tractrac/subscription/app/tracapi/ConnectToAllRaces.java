package com.tractrac.subscription.app.tracapi;

import com.tractrac.model.lib.api.ModelLocator;
import com.tractrac.model.lib.api.event.*;
import com.tractrac.subscription.lib.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ConnectToAllRaces {

    public static void main(String[] args) throws CreateModelException, URISyntaxException, SubscriberInitializationException, IOException, RaceLoadingException {

        URI paramURI = new URI("https://dev.tractrac.com/events/event_20240312_TrofeoPrin/jsonservice.php");
        //URI paramURI = new URI("http://192.168.0.11/events/event_20240312_TrofeoPrin/clientparams.php?event=event_20240312_TrofeoPrin&race=d9c0b480-d4e0-013c-1d82-0619092da6d7&random=459933703");
        IEventFactory eventFactory = ModelLocator.getEventFactory();
        IEvent event = eventFactory.createEvent(paramURI);


        ISubscriberFactory subscriberFactory = SubscriptionLocator.getSusbcriberFactory();
        for (IRace race : event.getRaces()) {
            race.reloadFromServer();
            for (int i = 0; i < 2; i++) {
                AbstractListener listener = new EventListener();

                IEventSubscriber eventSubscriber = subscriberFactory.createEventSubscriber(
                        event,
                        new URI("tcp://localhost:4400"),
                        new URI("tcp://localhost:4401")
                );
                eventSubscriber.subscribeConnectionStatus(listener);
                eventSubscriber.subscribeEventMessages(listener);
                eventSubscriber.subscribeRaces(listener);
                eventSubscriber.subscribeControls(listener);
                eventSubscriber.subscribeCompetitors(listener);

                IRaceSubscriber raceSubscriber = subscriberFactory.createRaceSubscriber(
                        race
                );
                raceSubscriber.subscribeConnectionStatus(listener);
                raceSubscriber.subscribeControlPositions(listener);
                raceSubscriber.subscribePositions(listener);
                //raceSubscriber.subscribePositionsSnapped(listener);
                raceSubscriber.subscribeControlPassings(listener);
                raceSubscriber.subscribeCompetitorSensorData(listener);
                raceSubscriber.subscribeRaceMessages(listener);
                raceSubscriber.subscribeRaceTimesChanges(listener);
                raceSubscriber.subscribeRouteChanges(listener);
                raceSubscriber.subscribeRaceCompetitor(listener);

                raceSubscriber.start();
                eventSubscriber.start();
            }
        }


        System.out.println("Press key to cancel live data stream");
        System.in.read();
        System.out.println("Cancelling data stream");
    }

}
