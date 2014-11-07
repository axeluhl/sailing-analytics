package com.sap.sailing.domain.racelog.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sap.sailing.domain.abstractlog.race.RaceLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventAuthorImpl;

public class RaceLogEventAuthorTest {
    
    @Test
    public void testCompareToSame() {
        RaceLogEventAuthor author = new RaceLogEventAuthorImpl("", 1);
        int result = author.compareTo(author);
        
        assertEquals(0, result);
    }
    
    @Test
    public void testCompareToMinor() {
        RaceLogEventAuthor author = new RaceLogEventAuthorImpl("", 1);
        RaceLogEventAuthor minor = new RaceLogEventAuthorImpl("", 2);
        int result = author.compareTo(minor);
        
        assertTrue(result > 0);
    }
    
    @Test
    public void testCompareToMajor() {
        RaceLogEventAuthor author = new RaceLogEventAuthorImpl("", 3);
        RaceLogEventAuthor major = new RaceLogEventAuthorImpl("", 2);
        int result = author.compareTo(major);
        
        assertTrue(result < 0);
    }

}
