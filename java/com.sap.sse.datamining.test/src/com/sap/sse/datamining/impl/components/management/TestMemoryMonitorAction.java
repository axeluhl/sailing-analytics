package com.sap.sse.datamining.impl.components.management;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.sap.sse.datamining.components.management.MemoryMonitorAction;
import com.sap.sse.datamining.test.util.components.management.Test_MemoryMonitorAction;

public class TestMemoryMonitorAction {
    
    @SuppressWarnings("unchecked")
    @Test
    public void testSimpleActionOrdering() {
        MemoryMonitorAction low = new Test_MemoryMonitorAction(0.8);
        MemoryMonitorAction medium = new Test_MemoryMonitorAction(0.5);
        MemoryMonitorAction high1 = new Test_MemoryMonitorAction(0.2);
        MemoryMonitorAction high2 = new Test_MemoryMonitorAction(0.2);
        List<MemoryMonitorAction> actions = Arrays.asList(low, high2, high1, medium);
        Collections.sort(actions);

        List<MemoryMonitorAction> expectedOrder1 = Arrays.asList(high1, high2, medium, low);
        List<MemoryMonitorAction> expectedOrder2 = Arrays.asList(high2, high1, medium, low);
        assertThat(actions, anyOf(is(expectedOrder1), is(expectedOrder2)));
    }
    
    @Test
    public void testActionPerforming() {
        Test_MemoryMonitorAction action = new Test_MemoryMonitorAction(0.2);

        assertThat(action.checkMemoryAndPerformAction(0.3), is(false));
        assertThat(action.actionHasBeenPerformed(), is(false));

        assertThat(action.checkMemoryAndPerformAction(0.2), is(false));
        assertThat(action.actionHasBeenPerformed(), is(false));

        assertThat(action.checkMemoryAndPerformAction(0.1), is(true));
        assertThat(action.actionHasBeenPerformed(), is(true));
    }

}
