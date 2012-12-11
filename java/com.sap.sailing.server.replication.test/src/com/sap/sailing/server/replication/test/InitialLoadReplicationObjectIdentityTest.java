package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.test.TrackBasedTest;
import com.sap.sailing.server.operationaltransformation.AddDefaultRegatta;
import com.sap.sailing.server.operationaltransformation.AddRaceDefinition;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;

public class InitialLoadReplicationObjectIdentityTest extends AbstractServerReplicationTest {
    private Pair<ReplicationServiceTestImpl, ReplicationMasterDescriptor> replicationDescriptorPair;
    
    /**
     * Drops the test DB. Sets up master and replica, starts the JMS message broker and registers the replica with the master.
     */
    @Before
    public void setUp() throws Exception {
        try {
            replicationDescriptorPair = basicSetUp(true, /* master=null means create a new one */ null,
            /* replica=null means create a new one */null);
        } catch (Throwable t) {
            tearDown();
        }
    }

    @Test
    public void testSameCompetitorInTwoRacesReplication() throws InterruptedException, ClassNotFoundException, IOException {
        final String boatClassName = "49er";
        // FIXME use master DomainFactory; see bug 592
        final DomainFactory masterDomainFactory = DomainFactory.INSTANCE;
        BoatClass boatClass = masterDomainFactory.getOrCreateBoatClass(boatClassName);
        final String baseEventName = "Test Event";
        AddDefaultRegatta addEventOperation = new AddDefaultRegatta(baseEventName, boatClassName);
        Regatta regatta = master.apply(addEventOperation);
        final String raceName1 = "Test Race 1";
        final String raceName2 = "Test Race 2";
        Competitor competitor = TrackBasedTest.createCompetitor("The Same Competitor");
        final CourseImpl masterCourse = new CourseImpl("Test Course", new ArrayList<Waypoint>());
        final ArrayList<Competitor> competitors = new ArrayList<Competitor>();
        competitors.add(competitor);
        RaceDefinition race1 = new RaceDefinitionImpl(raceName1, masterCourse, boatClass, competitors);
        // FIXME need to test initial load, not operation-based replication!!!
        AddRaceDefinition addRaceOperation1 = new AddRaceDefinition(new RegattaName(regatta.getName()), race1);
        master.apply(addRaceOperation1);
        RaceDefinition race2 = new RaceDefinitionImpl(raceName2, masterCourse, boatClass, competitors);
        AddRaceDefinition addRaceOperation2 = new AddRaceDefinition(new RegattaName(regatta.getName()), race2);
        master.apply(addRaceOperation2);
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        Regatta replicaEvent = replica.getRegatta(new RegattaName(regatta.getName()));
        RaceDefinition replicaRace1 = replicaEvent.getRaceByName(raceName1);
        RaceDefinition replicaRace2 = replicaEvent.getRaceByName(raceName2);
        assertSame(replicaRace1.getCompetitors().iterator().next(), replicaRace2.getCompetitors().iterator().next());
    }
}
