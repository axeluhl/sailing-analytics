package com.sap.sailing.datamining.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sap.sailing.datamining.Clusters;
import com.sap.sse.datamining.data.deprecated.ClusterOfComparable;
import com.sap.sse.datamining.impl.data.deprecated.ClusterOfComparableImpl;

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
