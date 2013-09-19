package com.sap.sailing.racecommittee.app.test;

import static org.junit.Assert.assertFalse;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.racegroup.RaceRow;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;
import com.sap.sailing.domain.base.racegroup.impl.RaceGroupImpl;
import com.sap.sailing.domain.base.racegroup.impl.SeriesWithRowsImpl;
import com.sap.sailing.racecommittee.app.domain.ManagedRaceIdentifier;
import com.sap.sailing.racecommittee.app.domain.impl.FleetIdentifierImpl;
import com.sap.sailing.racecommittee.app.domain.impl.ManagedRaceIdentifierImpl;

public class IdentifierTest {
    @Test
    public void testManagedRaceIdentifierUniqueness() {
        final Set<RaceRow> raceRows1 = Collections.emptySet();
        final SeriesWithRows series1 = new SeriesWithRowsImpl("ghi", /* isMedal */ false, /* raceRows */ raceRows1);
        final Set<SeriesWithRows> raceGroupSeries1 = Collections.singleton(series1);
        ManagedRaceIdentifier i1 = new ManagedRaceIdentifierImpl("abc", new FleetIdentifierImpl(new FleetImpl("def"), series1,
                new RaceGroupImpl("jkl.mno\\", new BoatClassImpl("505", /* typicallyStartsUpwind */ true), new CourseAreaImpl("Alpha", "Alpha ID"), raceGroupSeries1)));
        String i1ID = i1.getId().toString();
        
        final Set<RaceRow> raceRows2 = Collections.emptySet();
        final SeriesWithRows series2 = new SeriesWithRowsImpl("mno.ghi", /* isMedal */ false, /* raceRows */ raceRows2);
        final Set<SeriesWithRows> raceGroupSeries2 = Collections.singleton(series2);
        ManagedRaceIdentifier i2 = new ManagedRaceIdentifierImpl("abc", new FleetIdentifierImpl(new FleetImpl("def"), series2,
                new RaceGroupImpl("jkl\\", new BoatClassImpl("505", /* typicallyStartsUpwind */ true), new CourseAreaImpl("Alpha", "Alpha ID"), raceGroupSeries2)));
        String i2ID = i2.getId().toString();

        assertFalse(i1ID.equals(i2ID));
    }
}
