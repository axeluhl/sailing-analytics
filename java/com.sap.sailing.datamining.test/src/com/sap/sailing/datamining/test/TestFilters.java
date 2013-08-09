package com.sap.sailing.datamining.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.datamining.ClusterOfComparable;
import com.sap.sailing.datamining.Filter;
import com.sap.sailing.datamining.FilterCriteria;
import com.sap.sailing.datamining.impl.ClusterOfComparableImpl;
import com.sap.sailing.datamining.impl.FilterByCriteriaImpl;
import com.sap.sailing.datamining.impl.criterias.SimpleRangeFilterCriteria;

public class TestFilters {

    @Test
    public void testFilterByCriteria() {
        List<Integer> data = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        ClusterOfComparable<Integer> cluster = new ClusterOfComparableImpl<Integer>("Test", 5, 1);
        FilterCriteria<Integer> criteria = new SimpleRangeFilterCriteria<Integer>(cluster);
        Filter<Integer> filter = new FilterByCriteriaImpl<Integer>(criteria);
        Collection<Integer> expectedFilteredData = Arrays.asList(1, 2, 3, 4, 5);
        assertEquals(expectedFilteredData, filter.filter(data));
    }

}
