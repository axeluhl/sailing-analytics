package com.sap.sailing.domain.swisstimingadapter.impl;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.swisstimingadapter.RaceType;
import com.sap.sailing.domain.swisstimingadapter.RaceType.OlympicRaceCode;

public class DomainFactoryTest {
    
    private com.sap.sailing.domain.swisstimingadapter.impl.DomainFactoryImpl swissTimingDomainFactory;

    @Before
    public void before() {
        com.sap.sailing.domain.base.DomainFactory baseDomainFactory = new com.sap.sailing.domain.base.impl.DomainFactoryImpl((srlid)->null);
        swissTimingDomainFactory = new com.sap.sailing.domain.swisstimingadapter.impl.DomainFactoryImpl(
                baseDomainFactory);
    }
    
    @Test
    public void testGetFinnBoatClass() throws Exception {
        String raceID = "SAM002";
        RaceType raceType = swissTimingDomainFactory.getRaceTypeFromRaceID(raceID );
        assertThat(raceType, is(notNullValue()));
        assertThat(raceType.getRaceCode(), is(OlympicRaceCode.FINN));
    }
    
    @Test
    public void testGetFinnBoatClass_LowerCase() throws Exception {
        String raceID = "sam002";
        RaceType raceType = swissTimingDomainFactory.getRaceTypeFromRaceID(raceID );
        assertThat(raceType, is(notNullValue()));
        assertThat(raceType.getRaceCode(), is(OlympicRaceCode.FINN));
    }
    
    @Test
    public void testGet470WomenBoatClass() throws Exception {
        String raceID = "SAW005";
        RaceType raceType = swissTimingDomainFactory.getRaceTypeFromRaceID(raceID );
        assertThat(raceType, is(notNullValue()));
        assertThat(raceType.getRaceCode(), is(OlympicRaceCode._470_WOMEN));
    }
    
    @Test
    public void testDontGetWrongBoatClass() throws Exception {
        String raceID = "ABC";
        RaceType raceType = swissTimingDomainFactory.getRaceTypeFromRaceID(raceID );
        assertThat(raceType, is(notNullValue()));
        assertThat(raceType.getRaceCode(), is(OlympicRaceCode.UNKNOWN));
    }
}
