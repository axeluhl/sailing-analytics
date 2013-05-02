package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.base.impl.EventBaseImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;
import com.sap.sailing.domain.base.racegroup.impl.RaceGroupImpl;
import com.sap.sailing.domain.base.racegroup.impl.SeriesWithRowsImpl;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.StartProcedureType;
import com.sap.sailing.domain.racelog.PassAwareRaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.impl.PassAwareRaceLogImpl;
import com.sap.sailing.domain.racelog.impl.RaceLogEventFactoryImpl;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.impl.ManagedRaceIdentifierImpl;
import com.sap.sailing.racecommittee.app.domain.impl.ManagedRaceImpl;

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
        dataStore.addEvent(new EventBaseImpl("Extreme Sailing Series 2012 (Cardiff)", "Cardiff", "", true, "DUMBUUIDA"));
        dataStore.addEvent(new EventBaseImpl("Extreme Sailing Series 2012 (Nice)", "Nice", "", true, "DUMBUUIDB"));
        dataStore.addEvent(new EventBaseImpl("Extreme Sailing Series 2012 (Rio)", "Rio", "", true, "DUMBUUIDC"));
        EventBase newEvent = new EventBaseImpl("Extreme Sailing Series 2013 (Muscat)", "Muscat", "", true, "FIXUUID");
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
        
        List<Competitor> competitors = new ArrayList<Competitor>();
        competitors.add(new CompetitorImpl(UUID.randomUUID(), "SAP Extreme Sailing Team", null, null));
        competitors.add(new CompetitorImpl(UUID.randomUUID(), "The Wave Muscat", null, null));
        competitors.add(new CompetitorImpl(UUID.randomUUID(), "Red Bull Extreme Sailing Team", null, null));
        competitors.add(new CompetitorImpl(UUID.randomUUID(), "Team Korea", null, null));
        competitors.add(new CompetitorImpl(UUID.randomUUID(), "Realteam", null, null));

        RaceLogEventFactory factory = new RaceLogEventFactoryImpl();
        PassAwareRaceLog log = new PassAwareRaceLogImpl();
        log.add(factory.createStartTimeEvent(
                new MillisecondsTimePoint(new Date().getTime() - 2000), 
                1,
                new MillisecondsTimePoint(new Date().getTime() - 1000)));

        log.add(factory.createRaceStatusEvent(
                new MillisecondsTimePoint(new Date().getTime()), 
                1,
                RaceLogRaceStatus.FINISHING));

        ManagedRace q1 = new ManagedRaceImpl(
                new ManagedRaceIdentifierImpl(
                        "Q1", 
                        new FleetImpl("Default"), 
                        qualifying, 
                        raceGroup),
                        StartProcedureType.ESS,
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
                        StartProcedureType.ESS,
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
                        StartProcedureType.ESS,
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
        
        Mark m1 = new MarkImpl("Red");
        Mark m2 = new MarkImpl("Green");
        Mark m3 = new MarkImpl("White");
        
        dataStore.addMark(m1);
        dataStore.addMark(m2);
        dataStore.addMark(m3);
    }

    public void loadEvents(LoadClient<Collection<EventBase>> client) {
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

    @Override
    public void loadMarks(ManagedRace managedRace, LoadClient<Collection<Mark>> client) {
        client.onLoadSucceded(dataStore.getMarks());
    }

    @Override
    public void loadCourse(ManagedRace managedRace, LoadClient<CourseBase> client) {
        client.onLoadSucceded(managedRace.getCourseOnServer());
    }

    @Override
    public void loadCompetitors(ManagedRace managedRace, LoadClient<Collection<Competitor>> client) {
        client.onLoadSucceded((Collection<Competitor>) managedRace.getCompetitors());
    }

}
