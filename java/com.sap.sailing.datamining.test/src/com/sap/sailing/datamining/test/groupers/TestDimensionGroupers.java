package com.sap.sailing.datamining.test.groupers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.impl.AbstractDimension;
import com.sap.sailing.datamining.test.util.OpenDataReceiver;
import com.sap.sailing.datamining.test.util.OpenGrouper;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.CompoundGroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.workers.GroupingWorker;

public class TestDimensionGroupers {

    @Test
    public void testGroupByDimension() {
        Dimension<Integer, String> crossSum = createCrossSumDimension();
        GroupingWorker<Integer> groupByDimension = new OpenGrouper<Integer>(Arrays.asList(crossSum));
        
        OpenDataReceiver<Map<GroupKey, Collection<Integer>>> receiver = new OpenDataReceiver<>();
        groupByDimension.setReceiver(receiver);
        
        Collection<Integer> data = Arrays.asList(11, 2, 13, 4, 22, 3, 21, 111);
        groupByDimension.setDataToGroup(data);
        
        Map<GroupKey, Collection<Integer>> expectedGroups = new HashMap<GroupKey, Collection<Integer>>();
        Collection<Integer> group = new ArrayList<Integer>();
        group.add(11);
        group.add(2);
        expectedGroups.put(new GenericGroupKey<String>("2"), group);
        group = new ArrayList<Integer>();
        group.add(3);
        group.add(21);
        group.add(111);
        expectedGroups.put(new GenericGroupKey<String>("3"), group);
        group = new ArrayList<Integer>();
        group.add(13);
        group.add(4);
        group.add(22);
        expectedGroups.put(new GenericGroupKey<String>("4"), group);
        
        groupByDimension.run();
        assertEquals(expectedGroups, receiver.result);
    }
    
    @Test
    public void testGroupKeyGeneration() {
        Dimension<Integer, String> first = new AbstractDimension<Integer, String>("First") {
            @Override
            public String getDimensionValueFrom(Integer data) {
                return "First";
            }
        };
        Dimension<Integer, String> second = new AbstractDimension<Integer, String>("Second") {
            @Override
            public String getDimensionValueFrom(Integer data) {
                return "Second";
            }
        };
        Dimension<Integer, String> third = new AbstractDimension<Integer, String>("Third") {
            @Override
            public String getDimensionValueFrom(Integer data) {
                return "Third";
            }
        };
        
        OpenGrouper<Integer> grouper = new OpenGrouper<Integer>(Arrays.asList(first, second, third));
        GroupKey expectedGroupKey = new CompoundGroupKey(new GenericGroupKey<String>("First"), new CompoundGroupKey(new GenericGroupKey<String>("Second"), new GenericGroupKey<String>("Third")));
        assertEquals(expectedGroupKey, grouper.getGroupKey(1));
    }
    
    @Test
    public void testGroupByMultipleDimensions() {
        Dimension<Integer, String> crossSum = createCrossSumDimension();
        Dimension<Integer, String> signum = createSignumDimension();
        GroupingWorker<Integer> groupByDimensions = new OpenGrouper<Integer>(Arrays.asList(crossSum, signum));
        
        OpenDataReceiver<Map<GroupKey, Collection<Integer>>> receiver = new OpenDataReceiver<>();
        groupByDimensions.setReceiver(receiver);
        
        Collection<Integer> data = Arrays.asList(13, -4, 22, -3, 21, -111);
        groupByDimensions.setDataToGroup(data);

        Map<GroupKey, Collection<Integer>> expectedGroups = new HashMap<GroupKey, Collection<Integer>>();
        Collection<Integer> expectedGroup = new ArrayList<Integer>();
        expectedGroup.add(-3);
        expectedGroup.add(-111);
        expectedGroups.put(new CompoundGroupKey(new GenericGroupKey<String>("3"), new GenericGroupKey<String>("-1")), expectedGroup);
        expectedGroup = new ArrayList<Integer>();
        expectedGroup.add(13);
        expectedGroup.add(22);
        expectedGroups.put(new CompoundGroupKey(new GenericGroupKey<String>("4"), new GenericGroupKey<String>("1")), expectedGroup);
        expectedGroup = new ArrayList<Integer>();
        expectedGroup.add(21);
        expectedGroups.put(new CompoundGroupKey(new GenericGroupKey<String>("3"), new GenericGroupKey<String>("1")), expectedGroup);
        expectedGroup = new ArrayList<Integer>();
        expectedGroup.add(-4);
        expectedGroups.put(new CompoundGroupKey(new GenericGroupKey<String>("4"), new GenericGroupKey<String>("-1")), expectedGroup);
        
        groupByDimensions.run();
        Map<GroupKey, Collection<Integer>> groups = receiver.result;
        for (Entry<GroupKey, Collection<Integer>> expectedGroupEntry : expectedGroups.entrySet()) {
            Collection<Integer> group = groups.get(expectedGroupEntry.getKey());
            assertNotNull("No group for key: " + expectedGroupEntry.getKey().asString(), group);
            assertEquals(expectedGroupEntry.getValue(), group);
        }
    }

    private Dimension<Integer, String> createCrossSumDimension() {
        return new AbstractDimension<Integer, String>("Cross Sum") {
            @Override
            public String getDimensionValueFrom(Integer data) {
                int crossSum = 0;
                int value = Math.abs(data);
                while (value > 0) {
                    crossSum += value % 10;
                    value /= 10;
                }
                return crossSum + "";
            }
        };
    }

    private AbstractDimension<Integer, String> createSignumDimension() {
        return new AbstractDimension<Integer, String>("Signum") {
            @Override
            public String getDimensionValueFrom(Integer data) {
                return ((int) Math.signum(data)) + "";
            }
        };
    }

}
