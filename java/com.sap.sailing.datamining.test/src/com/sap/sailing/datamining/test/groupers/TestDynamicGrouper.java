package com.sap.sailing.datamining.test.groupers;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sap.sailing.datamining.Grouper;
import com.sap.sailing.datamining.impl.DynamicGrouper;
import com.sap.sailing.datamining.shared.GenericGroupKey;
import com.sap.sailing.datamining.shared.GroupKey;

public class TestDynamicGrouper {
    
    @Test
    public void testDynamicGrouper() {
        Collection<Data> data = Arrays.asList(new Data("One", 1), new Data("One", 11), new Data("Two", 2));
        String scriptText = "return data.getKey()";
        
        Grouper<Data> dynamicGrouper = new DynamicGrouper<Data>(scriptText);
        
        Map<GroupKey, Collection<Data>> expectedGroups = new HashMap<GroupKey, Collection<Data>>();
        Collection<Data> group = new ArrayList<Data>();
        group.add(new Data("Two", 2));
        expectedGroups.put(new GenericGroupKey<String>("Two"), group);
        group = new ArrayList<Data>();
        group.add(new Data("One", 1));
        group.add(new Data("One", 11));
        expectedGroups.put(new GenericGroupKey<String>("One"), group);
        
        assertEquals(expectedGroups, dynamicGrouper.group(data));
    }
    
    private static class Data {
        
        private String key;
        private Integer value;
        
        public Data(String key, Integer value) {
            this.key = key;
            this.value = value;
        }

        @SuppressWarnings("unused")
        public String getKey() {
            return key;
        }

        @SuppressWarnings("unused")
        public Integer getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Data other = (Data) obj;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }
        
    }

}
