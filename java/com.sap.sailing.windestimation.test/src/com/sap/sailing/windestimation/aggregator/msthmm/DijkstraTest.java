package com.sap.sailing.windestimation.aggregator.msthmm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.windestimation.aggregator.msthmm.graph.DijkstraShortestPathFinderImpl;
import com.sap.sailing.windestimation.aggregator.msthmm.graph.ElementWithQuality;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.NamedImpl;

public class DijkstraTest {
    private DijkstraShortestPathFinderImpl<Node> dijkstra;
    private Node startNode;
    private Node endNode;
    private Map<Node, Set<Node>> successors;
    private Map<Pair<Node, Node>, Double> edgeQuality;
    
    private static class Node extends NamedImpl implements ElementWithQuality {
        private static final long serialVersionUID = 6000367519447493367L;
        private final double quality;
        
        public Node(String name, double quality) {
            super(name);
            this.quality = quality;
        }

        @Override
        public double getQuality() {
            return quality;
        }
    }
    
    @Before
    public void setUp() {
        dijkstra = new DijkstraShortestPathFinderImpl<DijkstraTest.Node>();
        startNode = new Node("Start", 1.0);
        endNode = new Node("End", 1.0);
        successors = new HashMap<>();
        edgeQuality = new HashMap<>();
    }
    
    @Test
    public void testBasicDijkstra() {
        edge(startNode, endNode, 1.0);
        final Iterable<Node> path = getShortestPath();
        assertEquals(startNode, Util.get(path, 0));
        assertEquals(endNode, Util.get(path, 1));
    }

    private Iterable<Node> getShortestPath() {
        return dijkstra.getShortestPath(startNode, endNode,
                n -> successors.get(n), (n1, n2) -> edgeQuality.get(new Pair<>(n1, n2)));
    }

    @Test
    public void testDijkstraWithOneGoodAndOneBadPath() {
        final Node good = new Node("Good", 1.0);
        final Node bad = new Node("Bad", 0.5);
        edge(startNode, good, 1.0);
        edge(startNode, bad, 0.7);
        edge(good, endNode, 1.0);
        edge(bad, endNode, 0.7);
        final Iterable<Node> path = getShortestPath();
        assertEquals(startNode, Util.get(path, 0));
        assertEquals(good, Util.get(path, 1));
        assertEquals(endNode, Util.get(path, 2));
    }
    
    @Test
    public void testDijkstraWithGoodPathTurningBad() {
        final Node goodFirst = new Node("GoodFirst", 1.0);
        final Node badFirst = new Node("BadFirst", 0.5);
        edge(startNode, goodFirst, 1.0);
        edge(startNode, badFirst, 0.7);
        edge(goodFirst, endNode, 0.1);
        edge(badFirst, endNode, 1.0);
        final Iterable<Node> path = getShortestPath();
        assertEquals(startNode, Util.get(path, 0));
        assertEquals(badFirst, Util.get(path, 1));
        assertEquals(endNode, Util.get(path, 2));
    }
    
    @Test
    public void testNullForUnreachableEndNode() {
        assertNull(getShortestPath());
    }
    
    private void edge(Node from, Node to, double quality) {
        Util.addToValueSet(successors, from, to);
        edgeQuality.put(new Pair<>(from, to), quality);
    }
}
