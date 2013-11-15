package com.sap.sailing.datamining.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.datamining.ClusterOfComparable;
import com.sap.sailing.datamining.ConcurrentFilterCriteria;
import com.sap.sailing.datamining.FilterReceiver;
import com.sap.sailing.datamining.impl.ClusterOfComparableImpl;
import com.sap.sailing.datamining.impl.FilterByCriteria;
import com.sap.sailing.datamining.impl.SingleThreadedFilter;
import com.sap.sailing.datamining.impl.criterias.SimpleRangeFilterCriteria;

public class TestFilters {

    @Test
    public void testFilterByCriteria() {
        List<Integer> data = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        ClusterOfComparable<Integer> cluster = new ClusterOfComparableImpl<Integer>("Test", 5, 1);
        ConcurrentFilterCriteria<Integer> criteria = new SimpleRangeFilterCriteria<Integer>(cluster);
        SingleThreadedFilter<Integer> filter = new FilterByCriteria<Integer>(criteria);
        filter.setDataToFilter(data);

        Receiver receiver = new Receiver();
        filter.setReceiver(receiver);
        
        filter.run();
        Collection<Integer> expectedFilteredData = Arrays.asList(1, 2, 3, 4, 5);
        assertEquals(expectedFilteredData, receiver.data);
    }
    
    private class Receiver implements FilterReceiver<Integer> {
        
        public Collection<Integer> data;

        @Override
        public void addFilteredData(Collection<Integer> data) {
            this.data = data;
        }
        
    }

}
