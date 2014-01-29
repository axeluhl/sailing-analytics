package com.sap.sailing.datamining.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.sap.sailing.datamining.ClusterOfComparable;
import com.sap.sailing.datamining.Clusters;
import com.sap.sailing.datamining.impl.ClusterOfComparableImpl;

public class TestClusters {
    
    @Test
    public void testClusterOfComparable() {
        ClusterOfComparable<Integer> cluster = new ClusterOfComparableImpl<Integer>("Test", 3, 1); 
        
        assertTrue(cluster.isInRange(1)); 
        assertTrue(cluster.isInRange(2)); 
        assertTrue(cluster.isInRange(3));

        assertFalse(cluster.isInRange(null));
        assertFalse(cluster.isInRange(0));
        assertFalse(cluster.isInRange(4));
    }
    
    @Test
    public void testWindStrengthClusters() {
        assertNull(Clusters.WindStrength.getClusterFor(15, Clusters.WindStrength.StandardClusters));
        assertEquals(Clusters.WindStrength.VeryLight, Clusters.WindStrength.getClusterFor(1, Clusters.WindStrength.StandardClusters));
    }

}
