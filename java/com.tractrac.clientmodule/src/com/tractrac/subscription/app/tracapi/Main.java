package com.tractrac.subscription.app.tracapi;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.tractrac.model.lib.api.ModelLocator;
import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.model.lib.api.event.IEventFactory;
import com.tractrac.model.lib.api.event.IRace;
import com.tractrac.subscription.lib.api.IEventSubscriber;
import com.tractrac.subscription.lib.api.IRaceSubscriber;
import com.tractrac.subscription.lib.api.ISubscriberFactory;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;
import com.tractrac.subscription.lib.api.SubscriptionLocator;
import com.tractrac.util.lib.api.exceptions.TimeOutException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 
 * @author <a href="mailto:jorge@tractrac.dk">Jorge Piera Llodr&aacute</a>
 */
public class Main {

	/**
	 * @param args
	 *          the command line arguments
	 * @throws CreateModelException 
	 */
	public static void main(String[] args) throws URISyntaxException,
			MalformedURLException, FileNotFoundException, IOException,
			SubscriberInitializationException, CreateModelException, TimeOutException {

		URI paramURI = parseArguments(args);

		// Create the event object
		IEventFactory eventFactory = ModelLocator.getEventFactory();
		IRace race = eventFactory.createRace(paramURI);

		// Create the subscriber
		ISubscriberFactory subscriberFactory = SubscriptionLocator.getSusbcriberFactory();
		IEventSubscriber eventSubscriber = subscriberFactory.createEventSubscriber(race.getEvent());

		EventListener eventListener = new EventListener();
		eventSubscriber.subscribeConnectionStatus(eventListener);		
		eventSubscriber.subscribeEventMessages(eventListener);
		
		IRaceSubscriber raceSubscriber = subscriberFactory.createRaceSubscriber(race);
		raceSubscriber.subscribeConnectionStatus(eventListener);
		raceSubscriber.subscribeControlPositions(eventListener);
		raceSubscriber.subscribePositions(eventListener);
		raceSubscriber.subscribePositionsSnapped(eventListener);
		raceSubscriber.subscribeControlPassings(eventListener);
		raceSubscriber.subscribeRaceMessages(eventListener);
		raceSubscriber.subscribeRaceMessages(eventListener);
		raceSubscriber.subscribeRaceTimesChanges(eventListener);
		raceSubscriber.subscribeRouteChanges(eventListener);		
		raceSubscriber.start();		
		eventSubscriber.start();			
		
		// Go ahead with GUI or other stuff in main thread
		System.out.println("Press key to cancel live data stream");
		System.in.read();
		System.out.println("Cancelling data stream");
	
		// Stop data streams
		eventSubscriber.stop();		
		raceSubscriber.stop();	
	}

	private static URI parseArguments(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: java -jar TracAPI.jar parametersfile");
			System.exit(0);
		}
		try {
			return new URI(args[0]);
		} catch (URISyntaxException ex) {
			System.out.println("Malformed URL " + ex.getMessage());
			System.exit(0);
		}
		return null;
	}
}
