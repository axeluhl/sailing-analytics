package com.sap.sailing.domain.racelog.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventAuthorImpl;

public class RaceLogEventAuthorTest {
    
    @Test
    public void testCompareToSame() {
        AbstractLogEventAuthor author = new AbstractLogEventAuthorImpl("", 1);
        int result = author.compareTo(author);
        
        assertEquals(0, result);
    }
    
    @Test
    public void testCompareToMinor() {
        AbstractLogEventAuthor author = new AbstractLogEventAuthorImpl("", 1);
        AbstractLogEventAuthor minor = new AbstractLogEventAuthorImpl("", 2);
        int result = author.compareTo(minor);
        
        assertTrue(result > 0);
    }
    
    @Test
    public void testCompareToMajor() {
        AbstractLogEventAuthor author = new AbstractLogEventAuthorImpl("", 3);
        AbstractLogEventAuthor major = new AbstractLogEventAuthorImpl("", 2);
        int result = author.compareTo(major);
        
        assertTrue(result < 0);
    }

}
