package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.sap.sse.util.impl.ArrayListNavigableSet;

public class ArrayListNavigableSetTest {
    @Test
    public void testReplacementInHashSet() {
        Set<Integer> hashSet = new HashSet<Integer>();
        testSet(hashSet);
    }

    @Test
    public void testReplacementInArrayListNavigableSet() {
        Set<Integer> arrayListNavigableSet = new ArrayListNavigableSet<Integer>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        testSet(arrayListNavigableSet);
    }

    private void testSet(Set<Integer> hashSet) {
        Integer i1 = new Integer(1);
        Integer i2 = new Integer(1);
        assertNotSame(i1, i2);
        assertEquals(i1, i2);
        hashSet.add(i1);
        boolean replaced = hashSet.add(i2);
        assertFalse(replaced); // because s1 and s2 are equal
        Integer integerFromSet = hashSet.iterator().next();
        assertSame(i1, integerFromSet); // supposedly not replaced
    }
    
    @Test
    public void testMultiAdd() {
        Set<Integer> arrayListNavigableSet = new ArrayListNavigableSet<Integer>(Comparator.naturalOrder());        
        final List<Integer> elements = Arrays.asList(1, 2, 3, 4, 5);
        arrayListNavigableSet.addAll(elements);
        assertEquals(5, arrayListNavigableSet.size());
        assertEquals(new HashSet<>(elements), arrayListNavigableSet);
    }
}
