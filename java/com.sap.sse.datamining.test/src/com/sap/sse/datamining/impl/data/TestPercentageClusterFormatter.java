package com.sap.sse.datamining.impl.data;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.impl.data.LinearDoubleClusterGroup;
import com.sap.sse.datamining.impl.data.PercentageClusterFormatter;

public class TestPercentageClusterFormatter {

    private ClusterGroup<Double> percentageClusterGroup;
    private PercentageClusterFormatter formatter;
    
    @Before
    public void initialize() {
        percentageClusterGroup = new LinearDoubleClusterGroup(0.0, 1.0, 0.1, true);
        formatter = new PercentageClusterFormatter();
    }

    @Test
    public void testFormat() {
        Cluster<Double> cluster = percentageClusterGroup.getClusterFor(0.05);
        String formattedCluster = formatter.format(cluster);
        assertThat(formattedCluster, is("[0% - 10%["));
    }

}
