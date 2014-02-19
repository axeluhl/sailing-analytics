package com.sap.sailing.datamining.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.datamining.impl.CriteriaFiltrationWorker;
import com.sap.sailing.datamining.impl.criterias.SimpleRangeFilterCriteria;
import com.sap.sailing.datamining.test.util.OpenDataReceiver;
import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.data.ClusterOfComparable;
import com.sap.sse.datamining.impl.data.ClusterOfComparableImpl;
import com.sap.sse.datamining.workers.FiltrationWorker;

public class TestFilters {

    @Test
    public void testFilterByCriteria() {
        List<Integer> data = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        ClusterOfComparable<Integer> cluster = new ClusterOfComparableImpl<Integer>("Test", 5, 1);
        FilterCriteria<Integer> criteria = new SimpleRangeFilterCriteria<Integer>(cluster);
        FiltrationWorker<Integer> filter = new CriteriaFiltrationWorker<Integer>(criteria);
        filter.setDataToFilter(data);

        OpenDataReceiver<Collection<Integer>> receiver = new OpenDataReceiver<>();
        filter.setReceiver(receiver);
        
        filter.run();
        Collection<Integer> expectedFilteredData = Arrays.asList(1, 2, 3, 4, 5);
        assertEquals(expectedFilteredData, receiver.result);
    }

}
