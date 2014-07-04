package com.sap.sailing.domain.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import com.sap.sailing.domain.common.media.MediaUtil;

public class MediaUtilTest {

    @Test
    public void testCompareDatesAllNull() throws Exception {
        Date date1 = null;
        Date date2 = null;
        assertTrue(MediaUtil.compareDatesAllowingNull(date1, date2) == 0);
    }
    
    @Test
    public void testCompareDatesFirstNull() throws Exception {
        Date date1 = null;
        Date date2 = new Date();
        assertTrue(MediaUtil.compareDatesAllowingNull(date1, date2) < 0);
    }
    
    @Test
    public void testCompareDatesSecondNull() throws Exception {
        Date date1 = new Date();
        Date date2 = null;
        assertTrue(MediaUtil.compareDatesAllowingNull(date1, date2) > 0);
    }
    
    @Test
    public void testCompareDatesFirstGreaterSecond() throws Exception {
        Date date1 = new Date();
        Date date2 = new Date(date1.getTime() - 1);
        assertTrue(MediaUtil.compareDatesAllowingNull(date1, date2) > 0);
    }
    
    @Test
    public void testCompareDatesSecondGreaterFirst() throws Exception {
        Date date1 = new Date();
        Date date2 = new Date(date1.getTime() + 1);
        assertTrue(MediaUtil.compareDatesAllowingNull(date1, date2) < 0);
    }
    
    @Test
    public void testEqualsDatesAllNull() throws Exception {
        Date date1 = null;
        Date date2 = null;
        assertTrue(MediaUtil.equalsDatesAllowingNull(date1, date2));
    }
    
    @Test
    public void testEqualsDatesFirstNull() throws Exception {
        Date date1 = null;
        Date date2 = new Date();
        assertFalse(MediaUtil.equalsDatesAllowingNull(date1, date2));
    }
    
    @Test
    public void testEqualsDatesSecondNull() throws Exception {
        Date date1 = new Date();
        Date date2 = null;
        assertFalse(MediaUtil.equalsDatesAllowingNull(date1, date2));
    }
    
    @Test
    public void testEqualsDatesBothEqual() throws Exception {
        Date date1 = new Date();
        Date date2 = new Date(date1.getTime());
        assertTrue(MediaUtil.equalsDatesAllowingNull(date1, date2));
    }
    
    @Test
    public void testEqualsDatesNotEqual() throws Exception {
        Date date1 = new Date();
        Date date2 = new Date(date1.getTime() + 1);
        assertFalse(MediaUtil.equalsDatesAllowingNull(date1, date2));
    }
    
}
