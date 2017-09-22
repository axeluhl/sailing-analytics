package com.sap.sse.test;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.sap.sse.util.graph.DirectedEdge;
import com.sap.sse.util.graph.DirectedGraph;
import com.sap.sse.util.graph.impl.DirectedEdgeImpl;
import com.sap.sse.util.graph.impl.DirectedGraphImpl;
import com.sap.sse.util.topologicalordering.TopologicalComparator;

public class TopologicalComparatorTest {
    private final Set<String> nodes = new HashSet<>();
    private final Set<DirectedEdge<String>> edges = new HashSet<>();
    private DirectedGraph<String> graph;
    
    private void addLessThan(String lesser, String greater) {
        nodes.add(lesser);
        nodes.add(greater);
        edges.add(new DirectedEdgeImpl<String>(lesser, greater));
    }
    
    private void singleRoot(String root) {
        nodes.add(root);
    }
    
    private void createGraph() {
        graph = new DirectedGraphImpl<>(nodes, edges);
    }
    
    @Test
    public void testSimpleSort() {
        addLessThan("A", "B"); addLessThan("B", "C"); addLessThan("C", "D");
        addLessThan("E", "F"); addLessThan("F", "G"); addLessThan("G", "H");
        singleRoot("I");
        singleRoot("J");
        createGraph();
        TopologicalComparator<String> comparator = new TopologicalComparator<>(graph);
        assertMutualEquality(comparator, "A", "E", "I", "J");
        assertMutualEquality(comparator, "B", "F");
        assertMutualEquality(comparator, "C", "G");
        assertMutualEquality(comparator, "D", "H");
        assertChain(comparator, "A", "B", "C", "D");
        assertChain(comparator, "E", "F", "G", "H");
    }
    
    @Test
    public void testSimpleSortWithCycle() {
        addLessThan("A", "B"); addLessThan("B", "C"); addLessThan("C", "D"); addLessThan("D", "B"); addLessThan("D", "E");
        createGraph();
        TopologicalComparator<String> comparator = new TopologicalComparator<>(graph);
        assertMutualEquality(comparator, "B", "C", "D");
        assertChain(comparator, "A", "B", "E");
        assertChain(comparator, "A", "C", "E");
        assertChain(comparator, "A", "D", "E");
    }
    
    private void assertMutualEquality(TopologicalComparator<String> comparator, String... strings) {
        for (int i=0; i<strings.length; i++) {
            for (int j=0; j<strings.length; j++) {
                assertEquals(strings[i]+" at position "+i+" and "+strings[j]+" at position "+j+" compared incorrectly",
                        0, comparator.compare(strings[i], strings[j]));
            }
        }
    }
    
    private void assertChain(TopologicalComparator<String> comparator, String... strings) {
        for (int i=0; i<strings.length; i++) {
            for (int j=0; j<strings.length; j++) {
                assertEquals(strings[i]+" at position "+i+" and "+strings[j]+" at position "+j+" compared incorrectly",
                        Integer.compare(i, j), comparator.compare(strings[i], strings[j]));
            }
        }
    }
}
