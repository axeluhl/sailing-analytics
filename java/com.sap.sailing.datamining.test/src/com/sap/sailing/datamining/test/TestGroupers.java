package com.sap.sailing.datamining.test;

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
import com.sap.sailing.datamining.Grouper;
import com.sap.sailing.datamining.impl.AbstractDimension;
import com.sap.sailing.datamining.impl.CompoundGroupKey;
import com.sap.sailing.datamining.impl.GenericGroupKey;
import com.sap.sailing.datamining.impl.GroupByDimension;
import com.sap.sailing.datamining.shared.GroupKey;

public class TestGroupers {

    @Test
    public void testGroupByDimension() {
        Collection<Integer> data = Arrays.asList(11, 2, 13, 4, 22, 3, 21, 111);
        
        Dimension<Integer, String> crossSum = createCrossSumDimension();
        @SuppressWarnings("unchecked")
        Grouper<Integer> groupByDimension = createGrouper(crossSum);
        
        Map<GroupKey, Collection<Integer>> expectedGroups = new HashMap<GroupKey, Collection<Integer>>();
        Collection<Integer> group = new ArrayList<Integer>();
        group.add(11);
        group.add(2);
        expectedGroups.put(new StringGroupKey("2"), group);
        group = new ArrayList<Integer>();
        group.add(3);
        group.add(21);
        group.add(111);
        expectedGroups.put(new StringGroupKey("3"), group);
        group = new ArrayList<Integer>();
        group.add(13);
        group.add(4);
        group.add(22);
        expectedGroups.put(new StringGroupKey("4"), group);
        
        assertEquals(expectedGroups, groupByDimension.group(data));
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
        
        @SuppressWarnings("unchecked")
        OpenGrouper<Integer> grouper = new OpenGrouper<Integer>(first, second, third);
        GroupKey expectedGroupKey = new CompoundGroupKey(new StringGroupKey("First"), new CompoundGroupKey(new StringGroupKey("Second"), new StringGroupKey("Third")));
        assertEquals(expectedGroupKey, grouper.getGroupKey(1));
    }
    
    @Test
    public void testGroupByMultipleDimensions() {
        Collection<Integer> data = Arrays.asList(13, -4, 22, -3, 21, -111);
        
        Dimension<Integer, String> crossSum = createCrossSumDimension();
        Dimension<Integer, String> signum = createSignumDimension();
        @SuppressWarnings("unchecked")
        Grouper<Integer> groupByDimensions = createGrouper(crossSum, signum);

        Map<GroupKey, Collection<Integer>> expectedGroups = new HashMap<GroupKey, Collection<Integer>>();
        Collection<Integer> expectedGroup = new ArrayList<Integer>();
        expectedGroup.add(-3);
        expectedGroup.add(-111);
        expectedGroups.put(new CompoundGroupKey(new StringGroupKey("3"), new StringGroupKey("-1")), expectedGroup);
        expectedGroup = new ArrayList<Integer>();
        expectedGroup.add(13);
        expectedGroup.add(22);
        expectedGroups.put(new CompoundGroupKey(new StringGroupKey("4"), new StringGroupKey("1")), expectedGroup);
        expectedGroup = new ArrayList<Integer>();
        expectedGroup.add(21);
        expectedGroups.put(new CompoundGroupKey(new StringGroupKey("3"), new StringGroupKey("1")), expectedGroup);
        expectedGroup = new ArrayList<Integer>();
        expectedGroup.add(-4);
        expectedGroups.put(new CompoundGroupKey(new StringGroupKey("4"), new StringGroupKey("-1")), expectedGroup);
        
        Map<GroupKey, Collection<Integer>> groups = groupByDimensions.group(data);
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

    private GroupByDimension<Integer, String> createGrouper(Dimension<Integer, String>... dimensions) {
        return new OpenGrouper<Integer>(dimensions);
    }
    
    private class OpenGrouper<DataType> extends GroupByDimension<DataType, String> {

        public OpenGrouper(Dimension<DataType, String>... dimensions) {
            super(dimensions);
        }

        @Override
        protected GroupKey createGroupKeyFor(DataType dataEntry, Dimension<DataType, String> dimension) {
            return new StringGroupKey(dimension.getDimensionValueFrom(dataEntry));
        }
        
        public GroupKey getGroupKey(DataType dataEntry) {
            return getGroupKeyFor(dataEntry);
        }
        
    }
    
    public class StringGroupKey extends GenericGroupKey<String> {
        
        public StringGroupKey(String value) {
            super(value);
        }

        @Override
        public String asString() {
            return getValue();
        }

    }

}
