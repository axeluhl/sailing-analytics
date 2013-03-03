package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.EventData;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.base.impl.EventDataImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;
import com.sap.sailing.domain.base.racegroup.impl.RaceGroupImpl;
import com.sap.sailing.domain.base.racegroup.impl.SeriesWithRowsImpl;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.impl.RaceLogEventFactoryImpl;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.impl.ManagedRaceIdentifierImpl;
import com.sap.sailing.racecommittee.app.domain.impl.ManagedRaceImpl;
import com.sap.sailing.racecommittee.domain.racelog.PassAwareRaceLog;
import com.sap.sailing.racecommittee.domain.racelog.impl.PassAwareRaceLogImpl;

public class OfflineDataManager extends DataManager {
	
	private static boolean isInitialized = false;

	public OfflineDataManager(DataStore dataStore) {
		super(dataStore);
		
		if (!isInitialized) {
			isInitialized = true;
			fillDataStore(dataStore);
		}
	}

	private void fillDataStore(DataStore dataStore) {
		dataStore.addEvent(new EventDataImpl("Extreme Sailing Series 2012 (Cardiff)", "Cardiff", "", true, "DUMBUUIDA"));
		dataStore.addEvent(new EventDataImpl("Extreme Sailing Series 2012 (Nice)", "Nice", "", true, "DUMBUUIDB"));
		dataStore.addEvent(new EventDataImpl("Extreme Sailing Series 2012 (Rio)", "Rio", "", true, "DUMBUUIDC"));
		EventData newEvent = new EventDataImpl("Extreme Sailing Series 2013 (Muscat)", "Muscat", "", true, "FIXUUID");
		newEvent.getVenue().addCourseArea(new CourseAreaImpl("Offshore", "FIXCAUUID1"));
		newEvent.getVenue().addCourseArea(new CourseAreaImpl("Stadium", "FIXCAUUID2"));
		dataStore.addEvent(newEvent);
		
		SeriesWithRows qualifying = new SeriesWithRowsImpl("Qualifying", false, null);
		SeriesWithRows medal = new SeriesWithRowsImpl("Medal", true, null);
		RaceGroup raceGroup = new RaceGroupImpl(
				"ESS", 
				new BoatClassImpl("X40", false), 
				null,
				Arrays.asList(qualifying, medal));
		
		RaceLogEventFactory factory = new RaceLogEventFactoryImpl();
		PassAwareRaceLog log = new PassAwareRaceLogImpl();
		log.add(factory.createRaceStatusEvent(
				new MillisecondsTimePoint(new Date().getTime() + 1), 
				2,
				RaceLogRaceStatus.SCHEDULED));
		
		ManagedRace q1 = new ManagedRaceImpl(
				new ManagedRaceIdentifierImpl(
						"Q1", 
						new FleetImpl("Default"), 
						qualifying, 
						raceGroup), 
					log);
		
		log = new PassAwareRaceLogImpl();
		/*log.add(factory.createStartTimeEvent(
				new MillisecondsTimePoint(new Date()), 
				1,
				RaceLogRaceStatus.SCHEDULED, 
				new MillisecondsTimePoint(new Date().getTime() + 100000)));
		*/
		
		ManagedRace q2 = new ManagedRaceImpl(
				new ManagedRaceIdentifierImpl(
						"Q2", 
						new FleetImpl("Default"), 
						qualifying, 
						raceGroup), 
					log);
		
		log = new PassAwareRaceLogImpl();
		/*log.add(factory.createRaceStatusEvent(
				new MillisecondsTimePoint(new Date()), 
				5,
				RaceLogRaceStatus.FINISHED));*/
		ManagedRace q3 = new ManagedRaceImpl(
				new ManagedRaceIdentifierImpl(
						"Q3", 
						new FleetImpl("Default"), 
						qualifying, 
						raceGroup), 
					log);
		/*ManagedRace m1 = new ManagedRaceImpl(
				new ManagedRaceIdentifierImpl(
						"M1", 
						new FleetImpl("Default"), 
						medal, 
						raceGroup), 
				null);*/
		dataStore.addRace(q1);
		dataStore.addRace(q2);
		dataStore.addRace(q3);
		//dataStore.addRace(m1);
	}

	public void loadEvents(LoadClient<Collection<EventData>> client) {
		client.onLoadSucceded(dataStore.getEvents());
	}

	public void loadCourseAreas(Serializable parentEventId,
			LoadClient<Collection<CourseArea>> client) {
		client.onLoadSucceded(dataStore.getCourseAreas(dataStore.getEvent(parentEventId)));
	}

	public void loadRaces(Serializable courseAreaId,
			LoadClient<Collection<ManagedRace>> client) {
		client.onLoadSucceded(dataStore.getRaces());
	}

}
