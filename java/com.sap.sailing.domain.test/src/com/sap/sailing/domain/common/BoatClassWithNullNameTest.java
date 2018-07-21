package com.sap.sailing.domain.common;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.DomainFactory;

public class BoatClassWithNullNameTest {
    /**
     * See bug 2471
     */
    @Test
    public void testCreatingBoatClassWithNullName() {
        DomainFactory domainFactory = DomainFactory.INSTANCE;
        BoatClass boatClass = domainFactory.getOrCreateBoatClass(null);
        assertNotNull(boatClass);
        assertNull(boatClass.getName());
        BoatClass boatClass2 = domainFactory.getOrCreateBoatClass(null);
        assertSame(boatClass, boatClass2);
    }
}
