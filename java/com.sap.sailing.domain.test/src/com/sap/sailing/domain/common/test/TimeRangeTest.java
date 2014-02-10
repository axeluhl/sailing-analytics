package com.sap.sailing.domain.common.test;

import junit.framework.Assert;

import org.junit.Test;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.TimeRangeImpl;

public class TimeRangeTest {
    protected TimeRange create(long from, long to) {
        return new TimeRangeImpl(new MillisecondsTimePoint(from), new MillisecondsTimePoint(to));
    }
    
    protected TimePoint create(long millis) {
        return new MillisecondsTimePoint(millis);
    }
    
    private void liesWithin(long fromOuter, long toOuter, long fromInner, long toInner) {
        TimeRange outer = create(fromOuter, toOuter);
        TimeRange inner = create(fromInner, toInner);
        
        Assert.assertTrue(inner.liesWithin(outer));
        Assert.assertTrue(outer.includes(inner));
        Assert.assertFalse(inner.includes(outer));
        Assert.assertFalse(outer.liesWithin(inner));
    }
    
    @Test
    public void liesWithin() {
        liesWithin(0, 100, 10, 90);
        liesWithin(0, 100, 0, 90);
        liesWithin(0, 100, 10, 100);
    }
    
    @Test
    public void includesTimePoint() {
        TimeRange range = create(0, 100);
        Assert.assertTrue(range.includes(create(0)));
        Assert.assertTrue(range.includes(create(1)));
        Assert.assertTrue(range.includes(create(100)));
        Assert.assertFalse(range.includes(create(101)));
    }
    
    @Test
    public void intersects() {
        TimeRange one = create(0, 50);
        TimeRange two = create(20, 70);
        
        Assert.assertFalse(one.liesWithin(two));
        Assert.assertFalse(two.liesWithin(one));
        Assert.assertTrue(one.intersects(two));
        Assert.assertTrue(two.intersects(one));
    }
}
