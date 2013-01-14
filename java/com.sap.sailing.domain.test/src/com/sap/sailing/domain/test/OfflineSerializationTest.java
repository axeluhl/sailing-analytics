package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.DomainFactoryImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;

public class OfflineSerializationTest extends AbstractSerializationTest {
    /**
     * Bug 769 was based on an inconsistency of a cached hash code in Pair. The same problem existed for Triple.
     * Serialization changes the Object IDs of the objects contained and therefore the hash code based on this
     * identity. Serializing a cached hash code therefore leads to an inconsistency. The non-caching of this
     * hash code is tested here.
     */
    @Test
    public void testHashCodeOfSerializedPairIsConsistent() throws ClassNotFoundException, IOException {
        DomainFactory receiverDomainFactory = new DomainFactoryImpl();
        final Throwable s1 = new Throwable();
        final Throwable s2 = new Throwable();
        Pair<Throwable, Throwable> p =
                new Pair<Throwable, Throwable>(
                        s1, s2);
        HashSet<Pair<Throwable, Throwable>> s =
                new HashSet<Pair<Throwable, Throwable>>();
        s.add(p);
        Set<Pair<Throwable, Throwable>> ss =
                cloneBySerialization(s, /* resolveAgainst */ receiverDomainFactory);
        
        Pair<Throwable, Throwable> ps = ss.iterator().next();
        Throwable s1Des = ps.getA();
        Throwable s2Des = ps.getB();
        assertNotSame(s, ss);
        assertNotSame(s.iterator().next(), ss.iterator().next());
        assertNotSame(s1, s1Des);
        assertNotSame(s2, s2Des);
        assertEquals(1, ss.size());
        Pair<Throwable, Throwable> pNew =
                new Pair<Throwable, Throwable>(s1Des, s2Des);
        assertEquals(ps.hashCode(), pNew.hashCode());
        assertTrue(ss.contains(pNew));
    }
    
    /**
     * We had trouble de-serializing int[] through our specialized ObjectInputStream with its own resolveClass
     * implementation. This test failed initially before we changed the call for loading classes.
     */
    @Test
    public void testSerializingIntArray() throws ClassNotFoundException, IOException {
        DomainFactory receiverDomainFactory = new DomainFactoryImpl();
        int[] intArray = new int[] { 5, 8 };
        int[] clone = cloneBySerialization(intArray, receiverDomainFactory);
        assertTrue(Arrays.equals(intArray, clone));
    }
    
    /**
     * We had trouble de-serializing int[] through our specialized ObjectInputStream with its own resolveClass
     * implementation. This test failed initially before we changed the call for loading classes.
     */
    @Test
    public void testSerializingResultDiscardingRuleImpl() throws ClassNotFoundException, IOException {
        DomainFactory receiverDomainFactory = new DomainFactoryImpl();
        ResultDiscardingRuleImpl rdri = new ResultDiscardingRuleImpl(new int[] { 5, 8 });
        ResultDiscardingRuleImpl clone = cloneBySerialization(rdri, receiverDomainFactory);
        assertTrue(Arrays.equals(rdri.getDiscardIndexResultsStartingWithHowManyRaces(),
                clone.getDiscardIndexResultsStartingWithHowManyRaces()));
    }
    
    @Test
    public void testIdentityStabilityOfMarkSerialization() throws ClassNotFoundException, IOException {
        DomainFactory senderDomainFactory = new DomainFactoryImpl();
        DomainFactory receiverDomainFactory = new DomainFactoryImpl();
        Mark sendersMark1 = senderDomainFactory.getOrCreateMark("TestBuoy1");
        Mark receiversMark1 = cloneBySerialization(sendersMark1, receiverDomainFactory);
        Mark receiversSecondCopyOfMark1 = cloneBySerialization(sendersMark1, receiverDomainFactory);
        assertSame(receiversMark1, receiversSecondCopyOfMark1);
    }

    @Test
    public void testIdentityStabilityOfWaypointSerialization() throws ClassNotFoundException, IOException {
        DomainFactory senderDomainFactory = new DomainFactoryImpl();
        DomainFactory receiverDomainFactory = new DomainFactoryImpl();
        Mark sendersMark1 = senderDomainFactory.getOrCreateMark("TestBuoy1");
        Waypoint sendersWaypoint1 = senderDomainFactory.createWaypoint(sendersMark1);
        Waypoint receiversWaypoint1 = cloneBySerialization(sendersWaypoint1, receiverDomainFactory);
        Waypoint receiversSecondCopyOfWaypoint1 = cloneBySerialization(sendersWaypoint1, receiverDomainFactory);
        assertSame(receiversWaypoint1, receiversSecondCopyOfWaypoint1);
    }

    @Test
    public void testIdentityStabilityOfBoatClassSerialization() throws ClassNotFoundException, IOException {
        DomainFactory senderDomainFactory = new DomainFactoryImpl();
        DomainFactory receiverDomainFactory = new DomainFactoryImpl();
        BoatClass sendersBoatClass1 = senderDomainFactory.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */ true);
        BoatClass receiversBoatClass1 = cloneBySerialization(sendersBoatClass1, receiverDomainFactory);
        BoatClass receiversSecondCopyOfBoatClass1 = cloneBySerialization(sendersBoatClass1, receiverDomainFactory);
        assertSame(receiversBoatClass1, receiversSecondCopyOfBoatClass1);
    }

    @Test
    public void testIdentityStabilityOfNationalitySerialization() throws ClassNotFoundException, IOException {
        DomainFactory senderDomainFactory = new DomainFactoryImpl();
        DomainFactory receiverDomainFactory = new DomainFactoryImpl();
        Nationality sendersNationality1 = senderDomainFactory.getOrCreateNationality("GER");
        Nationality receiversNationality1 = cloneBySerialization(sendersNationality1, receiverDomainFactory);
        Nationality receiversSecondCopyOfNationality1 = cloneBySerialization(sendersNationality1, receiverDomainFactory);
        assertSame(receiversNationality1, receiversSecondCopyOfNationality1);
    }

    @Test
    public void testIdentityStabilityOfCompetitorSerialization() throws ClassNotFoundException, IOException {
        DomainFactory senderDomainFactory = new DomainFactoryImpl();
        DomainFactory receiverDomainFactory = new DomainFactoryImpl();
        String competitorName = "Tina Maximiliane Lutz";
        Competitor sendersCompetitor1 = new CompetitorImpl(123, competitorName, new TeamImpl("STG", Collections.singleton(
                new PersonImpl(competitorName, senderDomainFactory.getOrCreateNationality("GER"),
                /* dateOfBirth */ null, "This is famous "+competitorName)),
                new PersonImpl("Rigo van Maas", senderDomainFactory.getOrCreateNationality("GER"),
                /* dateOfBirth */null, "This is Rigo, the coach")), new BoatImpl(competitorName + "'s boat",
                        senderDomainFactory.getOrCreateBoatClass("470", /* typicallyStartsUpwind */ true), "GER 61"));
        Competitor receiversCompetitor1 = cloneBySerialization(sendersCompetitor1, receiverDomainFactory);
        Competitor receiversSecondCopyOfCompetitor1 = cloneBySerialization(sendersCompetitor1, receiverDomainFactory);
        assertSame(receiversCompetitor1, receiversSecondCopyOfCompetitor1);
    }
    
    @Test
    public void ensureSameObjectWrittenTwiceComesOutIdentical() throws ClassNotFoundException, IOException {
        final DomainFactoryImpl senderDomainFactory = new DomainFactoryImpl();
        DomainFactory receiverDomainFactory = new DomainFactoryImpl();
        Nationality n = senderDomainFactory.getOrCreateNationality("GER");
        Object[] copies = cloneManyBySerialization(receiverDomainFactory, n, n);
        assertEquals(2, copies.length);
        assertSame(copies[0], copies[1]);
        assertNotSame(n, copies[0]);
        assertEquals(n.getName(), ((Nationality) copies[0]).getName());
    }
}
