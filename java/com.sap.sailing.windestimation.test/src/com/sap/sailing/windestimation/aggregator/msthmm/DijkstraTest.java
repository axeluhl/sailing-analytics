package com.sap.sailing.windestimation.aggregator.msthmm;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sap.sailing.windestimation.aggregator.msthmm.graph.DijkstraShortestPathFinderImpl;
import com.sap.sailing.windestimation.aggregator.msthmm.graph.ElementWithQuality;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.NamedImpl;

public class DijkstraTest {
    private static class ElementWithQualityImpl extends NamedImpl implements ElementWithQuality {
        private static final long serialVersionUID = 6000367519447493367L;
        private final double quality;
        
        public ElementWithQualityImpl(String name, double quality) {
            super(name);
            this.quality = quality;
        }

        @Override
        public double getQuality() {
            return quality;
        }
    }
    
    @Test
    public void testBasicDijkstra() {
        final DijkstraShortestPathFinderImpl<ElementWithQualityImpl> dijkstra = new DijkstraShortestPathFinderImpl<DijkstraTest.ElementWithQualityImpl>();
        final ElementWithQualityImpl startNode = new ElementWithQualityImpl("Start", 1.0);
        final ElementWithQualityImpl endNode = new ElementWithQualityImpl("End", 1.0);
        final Map<ElementWithQualityImpl, Iterable<ElementWithQualityImpl>> successors = new HashMap<>();
        final Map<Pair<ElementWithQualityImpl, ElementWithQualityImpl>, Double> edgeQuality = new HashMap<>();
        successors.put(startNode, Collections.singleton(endNode));
        edgeQuality.put(new Pair<>(startNode, endNode), 1.0);
        final Iterable<ElementWithQualityImpl> path = dijkstra.getShortestPath(startNode, endNode,
                n -> successors.get(n), (n1, n2) -> edgeQuality.get(new Pair<>(n1, n2)));
        assertEquals(startNode, Util.get(path, 0));
        assertEquals(endNode, Util.get(path, 1));
    }
}
