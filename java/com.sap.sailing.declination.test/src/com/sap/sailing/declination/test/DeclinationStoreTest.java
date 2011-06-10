package com.sap.sailing.declination.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.declination.Declination;
import com.sap.sailing.declination.impl.DeclinationStore;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.util.QuadTree;

public class DeclinationStoreTest extends AbstractDeclinationTest {
    private DeclinationStore store;
    
    @Before
    public void setUp() {
        store = new DeclinationStore();
    }
    
    @Test
    public void testLoad2011() throws IOException, ClassNotFoundException, ParseException {
        QuadTree<Declination> declinationsFor2011 = store.getStoredDeclinations(2011);
        assertNotNull(declinationsFor2011);
        Declination declinationAt54N9E = declinationsFor2011.get(new DegreePosition(54, 9));
        assertEquals(1.+29./60., declinationAt54N9E.getBearing().getDegrees(), 0.00000001);
        assertEquals(0.+08./60., declinationAt54N9E.getAnnualChange().getDegrees(), 0.00000001);
    }

    @Test
    public void test1800IsNull() throws IOException, ClassNotFoundException, ParseException {
        QuadTree<Declination> declinationsFor1800 = store.getStoredDeclinations(1800);
        assertNull(declinationsFor1800);
    }
}
