package com.sap.sailinig.domain.based.impl;

import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.impl.SharedDomainFactoryImpl;
import com.sap.sailing.domain.common.BoatClassMasterdata;

/**
 * Tests creation and caching of boat classes. See also bug 3347
 * (http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=3347).
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class BoatClassCacheTest {
    private SharedDomainFactory sharedDomainFactory;
    
    @Before
    public void setUp() {
        sharedDomainFactory = new SharedDomainFactoryImpl(/* raceLogResolver */ null);
    }
    
    @Test
    public void testQueryingBoatClassByAlternativeNameFirstThenCanonicalizedAlternativeName() {
        final BoatClassMasterdata laserMasterData = BoatClassMasterdata.LASER_INT;
        final String laserAlternativeName = laserMasterData.getAlternativeNames()[0];
        final String unifiedAlternativeName = BoatClassMasterdata.unifyBoatClassName(laserAlternativeName);
        final BoatClass laserByDisplayName = sharedDomainFactory.getOrCreateBoatClass(laserMasterData.getDisplayName());
        final BoatClass laserByUnifiedAlternativeName = sharedDomainFactory.getOrCreateBoatClass(unifiedAlternativeName);
        assertSame(laserByDisplayName, laserByUnifiedAlternativeName);
    }
}
