package com.sap.see.pairinglist.test;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.pairinglist.PairingList;
import com.sap.sse.pairinglist.PairingListTemplate;
import com.sap.sse.pairinglist.impl.PairingListTemplateFactoryImpl;

import junit.framework.Assert;

public class PairingListTemplateFactoryTest {

    public PairingListTemplateFactoryImpl factoryImpl;
    
    @Before
    public void testSetup() {
        factoryImpl = new PairingListTemplateFactoryImpl();
    }
    
    /**
     * Checks if the Factory returns the same PairingListTemplate if one exists
     */
    @Test
    public void testGetOrCreate() {
        final int flights = 15;
        final int groups = 3;
        final int competitors = 18;
        
        PairingFrameProviderTest frameProviderTest = new PairingFrameProviderTest(flights, groups, competitors);
        PairingListTemplate list1 = factoryImpl.getOrCreatePairingListTemplate(frameProviderTest);
        PairingListTemplate list2 = factoryImpl.getOrCreatePairingListTemplate(frameProviderTest);
        if(list1!=list2) {
            Assert.fail("getOrCreatePairingListTemplate is not returning an already existing PairingListTemplate!");
        }
    }
    
    /**
     * Test some generations of example pairing combinations
     */
    @Test
    public void testGeneration() {
        PairingListTemplateFactoryImpl factoryImpl = new PairingListTemplateFactoryImpl();
        PairingListTemplate example1 = factoryImpl.getOrCreatePairingListTemplate(new PairingFrameProviderTest(15, 3, 18));
        PairingListTemplate example2 = factoryImpl.getOrCreatePairingListTemplate(new PairingFrameProviderTest(45, 6, 18));
        PairingListTemplate example3 = factoryImpl.getOrCreatePairingListTemplate(new PairingFrameProviderTest(10, 3, 30));
        assertNotNull(example1);
        assertNotNull(example2);
        assertNotNull(example3);
        
        /*
        for(int[] row : example1.getPairingListTemplate()) {
            System.out.println(Arrays.toString(row));
        }
        System.out.println(example1.getQuality());
        */
    }
    
    /**
     * Checks if the competitors of all single flights are divided up correctly (e.g. multiple competitors, absenced competitors)
     * flights, groups and competitors can be variable.
     */
    @Test
    public void checkFlights() {
        final int flights = 15;
        final int groups = 3;
        final int competitors = 18;
        
        ArrayList<Integer> availableCompetitors = new ArrayList<>();
        
        int[][] copy = factoryImpl.getOrCreatePairingListTemplate(new PairingFrameProviderTest(flights, groups, competitors)).getPairingListTemplate();
        
        for(int i = 0; i < flights; i++) {
            
            IntStream.range(1, competitors+1).forEach(competitor -> {
                availableCompetitors.add(competitor);
            });
            
            for(int j = 0; j < groups; j++) {
                for(int k = 0; k < competitors/groups; k++) {
                    if(availableCompetitors.contains(copy[i*groups+j][k])) {
                        availableCompetitors.remove(new Integer(copy[i*groups+j][k]));
                    }
                }
            }
            
            if(availableCompetitors.isEmpty()) {
                continue;
            } else {
                Assert.fail("The competitors are not divided up correctly!");
            }
        }
    }
}
