package com.sap.sailing.domain.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.tractracadapter.impl.DomainFactoryImpl;

public class DomainFactoryImplTest {
    private DomainFactoryImpl domainFactory;
    
    @Before
    public void setUp() {
        domainFactory  = new DomainFactoryImpl(DomainFactory.INSTANCE);        
    }
    
    
    @Test
    public void testGetDominantBoatClass_TestNullCollection() {
        Assert.assertEquals(null, domainFactory.getDominantBoatClass(null));
    }
    
    @Test
    public void testGetDominantBoatClass_TestEmptyCollection() {
        List<String> emptyList = new ArrayList<>();
        Assert.assertEquals(null, domainFactory.getDominantBoatClass(emptyList));
    }
    @Test
    public void testGetDominantBoatClass_TestWithOneObject() {
        List<String> list = new ArrayList<>();
        list.add("LSR");
        Assert.assertEquals("Laser Int.", domainFactory.getDominantBoatClass(list).getDisplayName());
    }
    
    @Test
    public void testGetDominantBoatClass_TestWithTheSameCount() {
        List<String> list = new ArrayList<>();
        list.add("LSR");
        list.add("18.Footer");
        String result = domainFactory.getDominantBoatClass(list).getDisplayName();
        if(!(result == "Laser Int." || result == "18Footer")) {
            Assert.assertEquals("Laser Int.", result);
        }
    }
    
    @Test
    public void testGetDominantBoatClass_TestWithMoreThanOneObject() {
        List<String> list = new ArrayList<>();
        list.add("LSR");
        list.add("18.Footer");
        list.add("LSR");
        String result = domainFactory.getDominantBoatClass(list).getDisplayName();
        Assert.assertEquals("Laser Int.", result);
    }
    
    @Test
    public void testGetDominantBoatClass_TestWithSynonymNames() {
        List<String> list = new ArrayList<>();
        list.add("LSR");
        list.add("18.Footer");
        list.add("18.Footer");
        list.add("Laser Int.");
        list.add("Laser");
        String result = domainFactory.getDominantBoatClass(list).getDisplayName();
        Assert.assertEquals("Laser Int.", result);
    }
}
