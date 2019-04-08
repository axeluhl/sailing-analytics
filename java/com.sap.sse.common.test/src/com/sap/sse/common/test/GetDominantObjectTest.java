package com.sap.sse.common.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.sap.sse.common.Util;

public class GetDominantObjectTest {

    @Test
    public void testNullCollection() {
        Assert.assertNull(Util.getDominantObject(null));               
    }
    
    @Test
    public void testEmptyCollection() {
        Iterable<String> objects = new ArrayList<>();
        Assert.assertEquals(null, Util.getDominantObject(objects));
    }
    
    @Test
    public void testCollectionWithOneObject() {
        List<String> objectCollection = new ArrayList<>();
        String dominant = "Test";
        objectCollection.add(dominant);
        Assert.assertEquals(dominant, Util.getDominantObject(objectCollection));
    }
    
    @Test
    public void testCollectionWithTheSameCount() {
        List<String> objectCollection = new ArrayList<>();
        String dominant = "Test";
        String dominantTwo = "Test2";
        objectCollection.add(dominant);
        objectCollection.add(dominantTwo);
        String result = Util.getDominantObject(objectCollection);
        if(!(result == dominant || result == dominantTwo)) {
            Assert.assertEquals(dominant, result);
        }            
    }
    
    @Test
    public void testCollectionWithMoreThenOneObject() {
        List<String> objectCollection = new ArrayList<>();
        String dominant = "Test";
        objectCollection.add("not Dominant");
        objectCollection.add(dominant);
        objectCollection.add(dominant);
        Assert.assertEquals(dominant, Util.getDominantObject(objectCollection));
    }
}
