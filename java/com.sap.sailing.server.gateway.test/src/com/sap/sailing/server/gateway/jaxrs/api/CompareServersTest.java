package com.sap.sailing.server.gateway.jaxrs.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sse.common.Util.Pair;

public class CompareServersTest {
    private CompareServersResource resource;
    
    @Before
    public void setUp() {
        resource = new CompareServersResource();
    }
    
    @Test
    public void testSimpleRemoveDuplicatesWithTwoEmptyObjects() {
        final JSONObject a = new JSONObject();
        final JSONObject b = new JSONObject();
        final Pair<Object, Object> result = resource.removeDuplicateEntries(a, b);
        assertNull(result.getA());
        assertNull(result.getB());
    }

    @Test
    public void testSimpleRemoveDuplicatesWithNameDifference() {
        final JSONObject a = new JSONObject();
        a.put("name", "a");
        final JSONObject b = new JSONObject();
        b.put("name", "b");
        final Pair<Object, Object> result = resource.removeDuplicateEntries(a, b);
        assertSame(a, result.getA());
        assertSame(b, result.getB());
        assertEquals("a", a.get("name"));
        assertEquals("b", b.get("name"));
    }

    @Test
    public void testSimpleRemoveDuplicatesWithEqualNames() {
        final JSONObject a = new JSONObject();
        a.put("name", "a");
        final JSONObject b = new JSONObject();
        b.put("name", "a");
        final Pair<Object, Object> result = resource.removeDuplicateEntries(a, b);
        assertNull(result.getA());
        assertNull(result.getB());
    }

    @Test
    public void testComparisonOfDiffsIfOrderingInArrayIsEqual() {
        final JSONObject a = new JSONObject();
        final JSONArray aArr = new JSONArray();
        a.put("array", aArr);
        aArr.add("123");
        aArr.add("234");
        final JSONObject b = new JSONObject();
        final JSONArray bArr = new JSONArray();
        b.put("array", bArr);
        bArr.add("123");
        bArr.add("234");
        final Pair<Object, Object> result = resource.removeDuplicateEntries(a, b);
        assertNull(result.getA());
        assertNull(result.getB());
    }

    @Test
    public void testComparisonOfDiffsIfOrderingInArrayIsChanged() {
        final JSONObject a = new JSONObject();
        final JSONArray aArr = new JSONArray();
        a.put("array", aArr);
        aArr.add("234");
        aArr.add("123");
        final JSONObject b = new JSONObject();
        final JSONArray bArr = new JSONArray();
        b.put("array", bArr);
        bArr.add("123");
        bArr.add("234");
        final Pair<Object, Object> result = resource.removeDuplicateEntries(a, b);
        assertNull(result.getA());
        assertNull(result.getB());
    }

    /**
     * A white-box test that asserts that different names should always lead to the object comparison and field
     * removal to be skipped, regardless of where in the order of keys "name" is found.
     */
    @Test
    public void testDifferentNameNotFirstField() {
        final JSONArray a = new JSONArray();
        final JSONArray b = new JSONArray();
        final JSONObject a1 = new JSONObject();
        a1.put("abc", "def");
        a1.put("isTracked", "jkl");
        a1.put("name", "a1");
        a.add(a1);
        final JSONObject b1 = new JSONObject();
        b1.put("name", "b1");
        b.add(b1);
        final JSONObject b2 = new JSONObject();
        b2.put("isTracked", "jkl");
        b2.put("name", "b2");
        b.add(b2);
        resource.removeDuplicateEntries(a, b);
        assertNull(b2.containsKey("isTracked"));
    }

    /**
     * A white-box test for something strange observed in the implementation for removing duplicate entries
     * when comparing arrays. It seems that if no equal match for an object from the first array is found in the second
     * then all elements from the second array will have their duplicates with that element from the first array removed
     * recursively.
     */
    @Test
    public void testAnotherStrangeArrayRecursion() {
        final JSONArray a = new JSONArray();
        final JSONArray b = new JSONArray();
        final JSONObject a1 = new JSONObject();
        a1.put("abc", "def");
        a1.put("ghi", "jkl");
        a1.put("isTracked", "jkl");
        a1.put("hasWindData", "123");
        a.add(a1);
        final JSONObject a2 = new JSONObject();
        a2.put("abc", "def");
        a2.put("ghi", "jkl");
        a2.put("isTracked", "vwx");
        a2.put("hasWindData", "234");
        a.add(a2);
        final JSONObject b1 = new JSONObject();
        b1.put("mno", "pqr");
        b1.put("isTracked", "stu");
        b.add(b1);
        final JSONObject b2 = new JSONObject();
        b2.put("isTracked", "jkl");
        b2.put("hasWindData", "234");
        b.add(b2);
        final JSONObject b3 = new JSONObject();
        b3.put("isTracked", "vwx");
        b3.put("hasWindData", "123");
        b.add(b3);
        resource.removeDuplicateEntries(a, b);
        assertNull(b2.containsKey("isTracked"));
    }
}
