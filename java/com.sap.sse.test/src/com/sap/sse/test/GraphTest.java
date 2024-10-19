package com.sap.sse.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.sap.sse.common.Util;
import com.sap.sse.util.graph.DirectedEdge;
import com.sap.sse.util.graph.DirectedGraph;

public class GraphTest {
    @Test
    public void testVerySimpleCycleDetection() {
        final String a = "a";
        final String b = "b";
        final Set<String> nodes = new HashSet<>();
        nodes.add(a);
        nodes.add(b);
        final Set<DirectedEdge<String>> edges = new HashSet<>();
        edges.add(DirectedEdge.create(a, b));
        edges.add(DirectedEdge.create(b, a));
        DirectedGraph<String> graph = DirectedGraph.create(nodes, edges);
        assertEquals(1, Util.size(graph.getCycleClusters().getClusters()));
        assertEquals(2, graph.getCycleClusters().getClusters().iterator().next().getClusterNodes().size());
    }

    @Test
    public void testCycleDetection() {
        final String cro = "CRO";
        final String usa = "USA";
        final String msc = "MSC";
        final String pol1 = "POL1";
        final String pol2 = "POL2";
        final String fin = "FIN";
        final String ger = "GER";
        final Set<String> nodes = new HashSet<>();
        nodes.add(cro);
        nodes.add(usa);
        nodes.add(msc);
        nodes.add(pol1);
        nodes.add(pol2);
        nodes.add(fin);
        nodes.add(ger);
        final List<DirectedEdge<String>> edgeList = new ArrayList<>();
        // CRO
        edgeList.add(DirectedEdge.create(cro, usa));
        edgeList.add(DirectedEdge.create(cro, msc));
        edgeList.add(DirectedEdge.create(cro, pol1));
        edgeList.add(DirectedEdge.create(cro, pol2));
        edgeList.add(DirectedEdge.create(cro, fin));
        edgeList.add(DirectedEdge.create(cro, ger));
        // USA
        edgeList.add(DirectedEdge.create(usa, msc));
        edgeList.add(DirectedEdge.create(usa, pol1));
        edgeList.add(DirectedEdge.create(usa, pol2));
        edgeList.add(DirectedEdge.create(usa, fin));
        edgeList.add(DirectedEdge.create(usa, ger));
        // MSC
        edgeList.add(DirectedEdge.create(msc, usa));
        edgeList.add(DirectedEdge.create(msc, pol2));
        edgeList.add(DirectedEdge.create(msc, fin));
        // POL1
        edgeList.add(DirectedEdge.create(pol1, fin));
        // POL2
        edgeList.add(DirectedEdge.create(pol2, fin));
        // FIN
        edgeList.add(DirectedEdge.create(fin, msc));
        // GER
        edgeList.add(DirectedEdge.create(ger, msc));
        edgeList.add(DirectedEdge.create(ger, pol1));
        edgeList.add(DirectedEdge.create(ger, pol2));
        edgeList.add(DirectedEdge.create(ger, fin));
        Collections.shuffle(edgeList);
        Set<DirectedEdge<String>> edges = new LinkedHashSet<>();
        edges.addAll(edgeList);
        DirectedGraph<String> graph = DirectedGraph.create(nodes, edges);
        assertEquals(1, Util.size(graph.getCycleClusters().getClusters()));
        assertEquals(6, graph.getCycleClusters().getClusters().iterator().next().getClusterNodes().size());
    }

    @Test
    public void testCycleDetectionWithDuplicateEdges() {
        final String cro = "CRO";
        final String usa = "USA";
        final String msc = "MSC";
        final String pol1 = "POL1";
        final String pol2 = "POL2";
        final String fin = "FIN";
        final String ger = "GER";
        final Set<String> nodes = new HashSet<>();
        nodes.add(cro);
        nodes.add(usa);
        nodes.add(msc);
        nodes.add(pol1);
        nodes.add(pol2);
        nodes.add(fin);
        nodes.add(ger);
        final Set<DirectedEdge<String>> edges = new HashSet<>();
        // CRO
        edges.add(DirectedEdge.create(cro, usa));
        edges.add(DirectedEdge.create(cro, msc));
        edges.add(DirectedEdge.create(cro, pol1));
        edges.add(DirectedEdge.create(cro, pol2));
        edges.add(DirectedEdge.create(cro, fin));
        edges.add(DirectedEdge.create(cro, ger));
        edges.add(DirectedEdge.create(cro, usa));
        edges.add(DirectedEdge.create(cro, msc));
        edges.add(DirectedEdge.create(cro, pol1));
        edges.add(DirectedEdge.create(cro, pol2));
        edges.add(DirectedEdge.create(cro, fin));
        edges.add(DirectedEdge.create(cro, ger));
        // USA
        edges.add(DirectedEdge.create(usa, msc));
        edges.add(DirectedEdge.create(usa, pol1));
        edges.add(DirectedEdge.create(usa, pol2));
        edges.add(DirectedEdge.create(usa, fin));
        edges.add(DirectedEdge.create(usa, ger));
        edges.add(DirectedEdge.create(usa, msc));
        edges.add(DirectedEdge.create(usa, pol1));
        edges.add(DirectedEdge.create(usa, pol2));
        edges.add(DirectedEdge.create(usa, fin));
        edges.add(DirectedEdge.create(usa, ger));
        // MSC
        edges.add(DirectedEdge.create(msc, usa));
        edges.add(DirectedEdge.create(msc, pol2));
        edges.add(DirectedEdge.create(msc, fin));
        edges.add(DirectedEdge.create(msc, usa));
        edges.add(DirectedEdge.create(msc, pol2));
        edges.add(DirectedEdge.create(msc, fin));
        // POL1
        edges.add(DirectedEdge.create(pol1, fin));
        edges.add(DirectedEdge.create(pol1, fin));
        // POL2
        edges.add(DirectedEdge.create(pol2, fin));
        edges.add(DirectedEdge.create(pol2, fin));
        // FIN
        edges.add(DirectedEdge.create(fin, msc));
        edges.add(DirectedEdge.create(fin, msc));
        // GER
        edges.add(DirectedEdge.create(ger, msc));
        edges.add(DirectedEdge.create(ger, pol1));
        edges.add(DirectedEdge.create(ger, pol2));
        edges.add(DirectedEdge.create(ger, fin));
        edges.add(DirectedEdge.create(ger, msc));
        edges.add(DirectedEdge.create(ger, pol1));
        edges.add(DirectedEdge.create(ger, pol2));
        edges.add(DirectedEdge.create(ger, fin));
        DirectedGraph<String> graph = DirectedGraph.create(nodes, edges);
        assertEquals(1, Util.size(graph.getCycleClusters().getClusters()));
        assertEquals(6, graph.getCycleClusters().getClusters().iterator().next().getClusterNodes().size());
    }
}
