package com.sap.sailing.windestimation.aggregator.advancedhmm;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.windestimation.aggregator.advancedhmm.AbstractAdvancedGraphGenerator.NodeWithDistance;
import com.sap.sailing.windestimation.aggregator.advancedhmm.AbstractAdvancedGraphGenerator.NodeWithNeighbors;
import com.sap.sse.common.Util.Pair;

public class AbstractAdvancedGraphGeneratorTest {

    private static final double DOUBLE_TOLERANCE = 0.0000001;

    private int counter;

    @Before
    public void beforeStart() {
        counter = 1;
    }

    @Test
    public void testOrderedInsert() {
        MockedAdvancedGraphGenerator generator = new MockedAdvancedGraphGenerator();
        generator.addNode(tuple(0, 0));
        generator.addNode(tuple(3, 3));
        generator.addNode(tuple(5, 5));
        assertMstCorrect(generator, 10);
    }

    @Test
    public void testWith3ElementsMiddleInsertedLast() {
        MockedAdvancedGraphGenerator generator = new MockedAdvancedGraphGenerator();
        generator.addNode(tuple(0, 0));
        generator.addNode(tuple(5, 5));
        generator.addNode(tuple(3, 3));
        assertMstCorrect(generator, 10);
    }

    @Test
    public void testCircle() {
        MockedAdvancedGraphGenerator generator = new MockedAdvancedGraphGenerator();
        generator.addNode(tuple(5, 0));
        generator.addNode(tuple(5, 10));
        generator.addNode(tuple(0, 5));
        generator.addNode(tuple(10, 5));
        assertMstCorrect(generator, 30);
    }

    @Test
    public void testCircleWithMiddle() {
        MockedAdvancedGraphGenerator generator = new MockedAdvancedGraphGenerator();
        generator.addNode(tuple(5, 0));
        generator.addNode(tuple(5, 10));
        generator.addNode(tuple(0, 5));
        generator.addNode(tuple(10, 5));
        generator.addNode(tuple(5, 5));
        assertMstCorrect(generator, 20);
    }

    @Test
    public void testAdvancedStuff() {
        MockedAdvancedGraphGenerator generator = new MockedAdvancedGraphGenerator();
        generator.addNode(tuple(0, 0)); // 1
        generator.addNode(tuple(10, 0)); // 2
        generator.addNode(tuple(20, 0)); // 3
        generator.addNode(tuple(5, 4)); // 4
        generator.addNode(tuple(14, 3)); // 5
        generator.addNode(tuple(9, 4)); // 6
        assertMstCorrect(generator, 33);
        // 9 (1->4) + 4 (4->6) + 5 (6->2) + 6 (6->5) + 9 (5->3) = 33
    }

    @Test
    public void testAdvancedStuff2() {
        MockedAdvancedGraphGenerator generator = new MockedAdvancedGraphGenerator();
        generator.addNode(tuple(5, 1)); // 1
        generator.addNode(tuple(4, 0)); // 2
        generator.addNode(tuple(12, 3)); // 3
        generator.addNode(tuple(10, 4)); // 4
        generator.addNode(tuple(8, 4)); // 5
        generator.addNode(tuple(6, 6)); // 6
        generator.addNode(tuple(2, 6)); // 7
        generator.addNode(tuple(3, 4)); // 8
        generator.addNode(tuple(5, 4)); // 9
        assertMstCorrect(generator, 21);
        // 2 (2->1) + 3 (1->9) + 2 (9->8) + 3 (8->7) + 3 (6->9) + 3 (9->5) + 2 (5->4) + 3 (4->3) = 21
    }

    @Test
    public void testCircleReverse() {
        MockedAdvancedGraphGenerator generator = new MockedAdvancedGraphGenerator();
        generator.addNode(tuple(5, 0)); // 1
        generator.addNode(tuple(9, 3)); // 2
        generator.addNode(tuple(7, 6)); // 3
        generator.addNode(tuple(3, 6)); // 4
        generator.addNode(tuple(2, 4)); // 5
        generator.addNode(tuple(3, 2)); // 6
        assertMstCorrect(generator, 19);
        // 4 (1->6) + 3 (6->5) + 3 (5->4) + 4 (4->3) + 5 (3->2) = 19
    }

    private void assertMstCorrect(MockedAdvancedGraphGenerator generator, double targetEdgesWeightSum) {
        double calculatedMstWeightSum = getWeightSumOfEdges(generator);
        assertEquals(targetEdgesWeightSum, calculatedMstWeightSum, DOUBLE_TOLERANCE);
    }

    private double getWeightSumOfEdges(MockedAdvancedGraphGenerator generator) {
        List<NodeWithNeighbors<Tuple>> nodes = generator.getNodes();
        NodeWithNeighbors<Tuple> firstNode = nodes.get(0);
        return getWeightSumOfEdges(firstNode, null);
    }

    private double getWeightSumOfEdges(NodeWithNeighbors<Tuple> node, NodeWithNeighbors<Tuple> previousNode) {
        double weightSum = 0;
        for (NodeWithDistance<Tuple> neighbor : node.getNeighbors()) {
            NodeWithNeighbors<Tuple> neighborNode = neighbor.getNodeWithNeighbors();
            if (neighborNode != previousNode) {
                System.out.println(node + " connected to " + neighbor);
                weightSum += neighbor.getDistance();
                weightSum += getWeightSumOfEdges(neighborNode, node);
            }
        }
        return weightSum;
    }

    private Tuple tuple(Integer a, Integer b) {
        return new Tuple(a, b);
    }

    private static class MockedAdvancedGraphGenerator extends AbstractAdvancedGraphGenerator<Tuple> {

        @Override
        protected double getDistanceBetweenObservations(Tuple o1, Tuple o2) {
            return Math.abs(o1.getA() - o2.getA()) + Math.abs(o1.getB() - o2.getB());
        }

    }

    private class Tuple extends Pair<Integer, Integer> {
        private static final long serialVersionUID = -8583758797034450326L;

        private int number = counter++;

        public Tuple(int a, int b) {
            super(a, b);
        }

        @Override
        public String toString() {
            return number + ": " + super.toString();
        }
    }

}
