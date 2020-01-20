/*
 * EventListener.java
 *
 * Created Oct 18, 2012 7:10:44 AM
 *
 * $Id$
 */
package com.tractrac.subscription.app.tracapi;

import com.tractrac.model.lib.api.data.*;
import com.tractrac.model.lib.api.event.*;
import com.tractrac.model.lib.api.route.IControl;
import com.tractrac.model.lib.api.route.IControlRoute;
import com.tractrac.model.lib.api.sensor.ISensorData;
import com.tractrac.subscription.lib.api.event.ILiveDataEvent;
import com.tractrac.subscription.lib.api.event.IStoredDataEvent;
import com.tractrac.util.lib.api.TimeUtils;

import java.net.URI;
import java.util.Date;
import java.util.UUID;

/**
 * 
 * @author <a href="mailto:jorge@tractrac.dk">Jorge Piera Llodr&aacute;</a>
 */
public class EventListener extends AbstractListener {

	private static void show(Object obj) {
		System.out.println(String.valueOf(TimeUtils.formatDateInMillis(new Date().getTime())) + ": " + obj);
	}

	private static long controlPos = 0;
	private static long compPos = 0;
		
	@Override
	public void gotStoredDataEvent(IStoredDataEvent storedDataEvent) {
		show(storedDataEvent);
		if (storedDataEvent.getType() == IStoredDataEvent.Type.End) {
			System.out.println("CONTROLS: " + controlPos);
			System.out.println("COMPETITORS: " + compPos);
		}
	}

	@Override
	public void gotLiveDataEvent(ILiveDataEvent liveDataEvent) {
		show(liveDataEvent);
	}

	@Override
	public void gotRouteChange(IControlRoute controlRoute, long timeStamp) {
		StringBuilder message = new StringBuilder();
		message.append("New route at " + timeStamp + ": " + controlRoute.toString());
		for (int i=0 ; i<controlRoute.getControls().size() ; i++) {
			message.append("\n\t" + i + ": " + controlRoute.getControls().get(i).getName());
		}
		message.append("\n\t" + controlRoute.getMetadata().getText());
		show(message);
	}

	@Override
	public void gotControlPassings(IRaceCompetitor raceCompetitor,
			IControlPassings markPassings) {
		//show("New markpassings " + markPassings + " for the competitor " + raceCompetitor.getCompetitor().toString());

	}

	@Override
	public void gotControlPointPosition(IControl control, IPosition position, int markNumber) {
		StringBuilder message = new StringBuilder();
		message.append("COTRLPOS " + markNumber);
		message.append("\t").append(control.getShortName()).append("\t");
		message.append(position.toString());
		show(message.toString());
		controlPos++;
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
		StringBuilder message = new StringBuilder();
		message.append("POSITION");
		if (raceCompetitor != null) {
			message.append("\t").append(raceCompetitor.getCompetitor().getShortName()).append("\t");
		}
		message.append(position.toString());
	//	show(message.toString());
		compPos++;
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

	@Override
	public void gotSensorData(IRaceCompetitor raceCompetitor, ISensorData sensorData) {
		StringBuilder message = new StringBuilder();
		message.append("SENSOR");
		if (raceCompetitor != null) {
			message.append("\t").append(raceCompetitor.getCompetitor().getShortName()).append("\t");
		}
		message.append(sensorData.toString());
		show(message.toString());
	}

	@Override
	public void updateCompetitor(ICompetitor competitor) {
		show("UPDATE COMPETITOR " + competitor.toString());
	}

	@Override
	public void addCompetitor(ICompetitor competitor) {
		show("ADD COMPETITOR " + competitor.toString());
	}

	@Override
	public void deleteCompetitor(UUID competitorId) {
		show("DELETE COMPETITOR");
	}

	@Override
	public void updateControl(IControl control) {
		show("UPDATE CONTROL " + control.toString());
	}

	@Override
	public void addControl(IControl control) {
		show("ADD CONTROL " + control.toString());
	}

	@Override
	public void deleteControl(UUID controlId) {
		show("DELETE CONTROL " + controlId);
	}

	@Override
	public void addRaceCompetitor(IRaceCompetitor raceCompetitor) {
		show("ADD RACE COMPETITOR " + raceCompetitor.toString());
	}

	@Override
	public void updateRaceCompetitor(IRaceCompetitor raceCompetitor) {
		show("UPDATE RACE COMPETITOR " + raceCompetitor.toString());
	}

	@Override
	public void deleteRaceCompetitor(UUID competitorId) {
		show("DELETE RACE COMPETITOR " + competitorId);
	}


	@Override
	public void updateRace(IRace race) {
		show("UPDATE RACE " + race.toString() +
				"[" + TimeUtils.formatDate(race.getTrackingStartTime()) +
				"," + TimeUtils.formatDate(race.getTrackingEndTime()) + "] - " +
				TimeUtils.formatDate(race.getRaceStartTime()) +
				" liveDelay = " + race.getLiveDelay()
		);
	}

	@Override
	public void addRace(IRace race) {
		show("ADD RACE " + race.toString());
	}

	@Override
	public void deleteRace(UUID raceId) {
		show("DELETE RACE " + raceId);
	}

	@Override
	public void reloadRace(UUID raceId) {
		show("RELOAD RACE " + raceId);
	}

	@Override
	public void abandonRace(UUID raceId) {
		show("ABANDON RACE " + raceId);
	}

	@Override
	public void startTracking(UUID raceId) {
		show("START TRACKING " + raceId);
	}

	@Override
	public void dataSourceChanged(IRace race, DataSource oldDataSource, URI oldLiveURI, URI oldStoredURI) {
		show("DATA SOURCE CHANGE " + race);
	}
}
