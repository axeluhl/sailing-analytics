package com.sap.sse.common.test;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.sap.sse.common.Util;

public class GetDominantObjektTest {

    @Test
    public void testNullCollection() {
        Assert.assertArrayEquals(null, Util.getDominantObjekt(null));               
    }
    
    @Test
    public void testEmptyCollection() {
        Iterable<String> objects = new ArrayList<String>();
        Assert.assertEquals(null, Util.getDominantObjekt(objects));
    }
    
    @Test
    public void testCollectionWithOneObject() {
        ArrayList<String> objectCollection = new ArrayList<String>();
        String dominant = "Test";
        objectCollection.add(dominant);
        Assert.assertEquals(dominant, Util.getDominantObjekt(objectCollection));
    }
    
    @Test
    public void testCollectionWithTheSameCount() {
        ArrayList<String> objectCollection = new ArrayList<String>();
        String dominant = "Test";
        String dominantTwo = "Test2";
        objectCollection.add(dominant);
        objectCollection.add(dominantTwo);
        String result = Util.getDominantObjekt(objectCollection);
        if(!(result == dominant || result == dominantTwo)) {
            Assert.assertEquals(dominant, result);
        }            
    }
    
    @Test
    public void testCollectionWithMoreThenOneObject() {
        ArrayList<String> objectCollection = new ArrayList<String>();
        String dominant = "Test";
        objectCollection.add("not Dominant");
        objectCollection.add(dominant);
        objectCollection.add(dominant);
        Assert.assertEquals(dominant, Util.getDominantObjekt(objectCollection));
    }
}
