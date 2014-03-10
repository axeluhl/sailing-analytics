package com.sap.sse.datamining.impl.data;

import static org.junit.Assert.*;

import java.util.Comparator;

import org.junit.Ignore;
import org.junit.Test;

import com.sap.sse.datamining.data.ClusterGroup;

public class TestFixClusterGroup {

    public static final Comparator<Integer> comparator = new ComparableComparator<Integer>();

    @Ignore
    @Test
    public void testClusterGroupWithClosedBoundaries() {
        ClusterGroup<Integer> groupWithClosedBoundaries = createGroupWithClosedBoundaries();
        fail("Not yet implemented");
    }

    private ClusterGroup<Integer> createGroupWithClosedBoundaries() {
        // TODO Auto-generated method stub
        return null;
    }

}
