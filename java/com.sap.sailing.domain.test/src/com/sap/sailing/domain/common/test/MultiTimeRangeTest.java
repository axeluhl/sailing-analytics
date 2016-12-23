package com.sap.sailing.domain.common.test;

import static com.sap.sailing.domain.common.test.TimeTestHelpers.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.sap.sse.common.MultiTimeRange;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MultiTimeRangeImpl;

public class MultiTimeRangeTest {
    private MultiTimeRange createMulti(long... startAndEndSequence) {
        assert startAndEndSequence != null;
        assert startAndEndSequence.length % 2 == 0;
        List<TimeRange> list = new ArrayList<>();
        for (int i=0; i<startAndEndSequence.length; i+=2) {
            list.add(create(startAndEndSequence[i], startAndEndSequence[i+1]));
        }
        return new MultiTimeRangeImpl(list);
    }
    
    @Test
    public void testEmptyMultiTimeRange() {
        assertTrue(createMulti().isEmpty());
    }
    
    @Test
    public void testAdd() {
        MultiTimeRange mtr = createMulti(100, 200);
        MultiTimeRange multi = mtr.add(createMulti(300, 400));
        assertEquals(2, Util.size(multi));
        assertEquals(mtr.iterator().next(), multi.iterator().next());
        assertTrue(multi.includes(create(150)));
        assertTrue(multi.includes(create(350)));
        assertFalse(multi.includes(create(250)));
        assertFalse(multi.includes(create(200))); // exclusive first time range
        assertFalse(multi.includes(create(400))); // exclusive last time range
    }
}
