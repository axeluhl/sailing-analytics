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
 * @author <a href="mailto:jorge@tractrac.dk">Jorge Piera Llodr&aacute;</a>
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

		Object[] myArgs = parseArguments(args);
		URI paramURI = (URI)myArgs[0];
		boolean measureDelay = (boolean)myArgs[1];;

		// Create the event object
		IEventFactory eventFactory = ModelLocator.getEventFactory();
		IRace race = eventFactory.createRace(paramURI);

		// Create the subscriber
		ISubscriberFactory subscriberFactory = SubscriptionLocator.getSusbcriberFactory();
		IEventSubscriber eventSubscriber = subscriberFactory.createEventSubscriber(race.getEvent());

		AbstractListener listener;
		if (measureDelay) {
			listener = new DelayListener();
		} else {
			listener = new EventListener();
		}

		eventSubscriber.subscribeConnectionStatus(listener);
		eventSubscriber.subscribeEventMessages(listener);
		eventSubscriber.subscribeRaces(listener);
		eventSubscriber.subscribeControls(listener);
		eventSubscriber.subscribeCompetitors(listener);

		IRaceSubscriber raceSubscriber = subscriberFactory.createRaceSubscriber(race);
		raceSubscriber.subscribeConnectionStatus(listener);
		raceSubscriber.subscribeControlPositions(listener);
		raceSubscriber.subscribePositions(listener);
//		raceSubscriber.subscribePositionsSnapped(listener);
		raceSubscriber.subscribeControlPassings(listener);
		raceSubscriber.subscribeCompetitorSensorData(listener);
		raceSubscriber.subscribeRaceMessages(listener);
		raceSubscriber.subscribeRaceTimesChanges(listener);
		raceSubscriber.subscribeRouteChanges(listener);
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

	private static Object[] parseArguments(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: java -jar TracAPI.jar parametersfile measureDelay");
			System.exit(0);
		}
		Object[] myArgs = new Object[2];
		try {
			myArgs[0] = new URI(args[0]);
			myArgs[1] = args.length >= 2 && args[1].equals("1");
		} catch (URISyntaxException ex) {
			System.out.println("Malformed URL " + ex.getMessage());
			System.exit(0);
		}
		return myArgs;
	}
}
