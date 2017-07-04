/*
 * EventListener.java
 *
 * Created Oct 18, 2012 7:10:44 AM
 *
 * $Id$
 */
package com.tractrac.subscription.app.tracapi;

import com.tractrac.model.lib.api.data.IControlPassings;
import com.tractrac.model.lib.api.data.IMessageData;
import com.tractrac.model.lib.api.data.IPosition;
import com.tractrac.model.lib.api.data.IPositionOffset;
import com.tractrac.model.lib.api.data.IPositionSnapped;
import com.tractrac.model.lib.api.data.IStartStopData;
import com.tractrac.model.lib.api.event.IEvent;
import com.tractrac.model.lib.api.event.IRace;
import com.tractrac.model.lib.api.event.IRaceCompetitor;
import com.tractrac.model.lib.api.route.IControl;
import com.tractrac.model.lib.api.route.IControlRoute;
import com.tractrac.subscription.lib.api.competitor.IPositionListener;
import com.tractrac.subscription.lib.api.competitor.IPositionOffsetListener;
import com.tractrac.subscription.lib.api.competitor.IPositionSnappedListener;
import com.tractrac.subscription.lib.api.control.IControlPassingsListener;
import com.tractrac.subscription.lib.api.control.IControlPointPositionListener;
import com.tractrac.subscription.lib.api.control.IControlRouteChangeListener;
import com.tractrac.subscription.lib.api.event.IConnectionStatusListener;
import com.tractrac.subscription.lib.api.event.IEventMessageListener;
import com.tractrac.subscription.lib.api.event.ILiveDataEvent;
import com.tractrac.subscription.lib.api.event.IStoredDataEvent;
import com.tractrac.subscription.lib.api.race.IRaceMessageListener;
import com.tractrac.subscription.lib.api.race.IRaceStartStopTimesChangeListener;

/**
 * 
 * @author <a href="mailto:jorge@tractrac.dk">Jorge Piera Llodr&aacute</a>
 */
public class EventListener implements IEventMessageListener,
		IRaceMessageListener, IPositionListener, IPositionOffsetListener,
		IPositionSnappedListener, IConnectionStatusListener, IControlPointPositionListener,
		IControlPassingsListener, IRaceStartStopTimesChangeListener,
		IControlRouteChangeListener {

	private static void show(Object obj) {
		System.out.println(Thread.currentThread().getName() + ": " + obj);
	}
		
	@Override
	public void gotStoredDataEvent(IStoredDataEvent storedDataEvent) {
		show(storedDataEvent);
	}

	@Override
	public void gotLiveDataEvent(ILiveDataEvent liveDataEvent) {
		show(liveDataEvent);
	}

	@Override
	public void gotRouteChange(IControlRoute controlRoute, long timeStamp) {
		show("New route at " + timeStamp + ": " + controlRoute.toString());		
	}

	@Override
	public void gotControlPassings(IRaceCompetitor raceCompetitor,
			IControlPassings markPassings) {
		show("New markpassings " + markPassings + " for the competitor " + raceCompetitor.getCompetitor().toString());				
	}

	@Override
	public void gotControlPointPosition(IControl control, IPosition position, int markNumber) {
		show("New position " + position + " for the mark " + control + ", " + markNumber);			
	}

	@Override
	public void gotPositionSnapped(IRaceCompetitor raceCompetitor,
			IPositionSnapped positionSnapped) {
		show("New position " + positionSnapped + " for the competitor " + raceCompetitor.getCompetitor().toString());				
	}

	@Override
	public void gotPositionOffset(IRaceCompetitor raceCompetitor,
			IPositionOffset position) {
		show("New position " + position + " for the competitor " + raceCompetitor.getCompetitor().toString());						
	}

	@Override
	public void gotPosition(IRaceCompetitor raceCompetitor, IPosition position) {
		show("New position " + position + " for the competitor " + raceCompetitor.getCompetitor().toString());								
	}

	@Override
	public void gotRaceStartStopTime(IRace race, IStartStopData startStopData) {
		show("New race start/stop times " + startStopData.toString());										
	}

	@Override
	public void gotTrackingStartStopTime(IRace race, IStartStopData startStopData) {
		show("New tracking race start/stop times " + startStopData.toString());				
	}

	@Override
	public void gotRaceMessage(IRace race, IMessageData messageData) {
		show("New race message " + messageData.toString());		
	}

	@Override
	public void stopped(Object object) {
		show("Stopping the connection with " + object.toString());		
	}

	@Override
	public void gotEventMessage(IEvent event, IMessageData messageData) {
		show("New event message " + messageData.toString());				
	}
}
