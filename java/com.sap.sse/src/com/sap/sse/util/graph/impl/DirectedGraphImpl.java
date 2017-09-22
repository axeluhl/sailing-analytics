package com.sap.sse.util.graph.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.util.graph.CycleCluster;
import com.sap.sse.util.graph.CycleClusters;
import com.sap.sse.util.graph.DirectedEdge;
import com.sap.sse.util.graph.DirectedGraph;
import com.sap.sse.util.graph.Path;

public class DirectedGraphImpl<T> implements DirectedGraph<T> {
    private final Set<T> nodes;
    private final Set<DirectedEdge<T>> edges;
    private final Map<T, Set<T>> immediateSuccessors;
    private final Set<T> roots;
    
    /**
     * A node may be part of one or more cycles. Contains a key only if it is part of at least one cycle.
     * In other words, the value sets are never empty, and the key set describes all nodes that are part
     * of at least one cycle.
     */
    private final Map<T, Set<Path<T>>> cyclesPerNode;
    
    private final CycleClusters<T> cycleClusters;
    
    private final Set<Path<T>> cycles;
    
    public DirectedGraphImpl(Set<T> nodes, Set<DirectedEdge<T>> edges) {
        this.nodes = nodes;
        this.edges = edges;
        final Map<T, Set<T>> succ = new HashMap<>();
        final Map<T, Set<T>> pred = new HashMap<>();
        for (final T node : nodes) {
            succ.put(node, new HashSet<>());
            pred.put(node, new HashSet<>());
        }
        for (final DirectedEdge<T> e : edges) {
            succ.get(e.getFrom()).add(e.getTo());
            pred.get(e.getTo()).add(e.getFrom());
        }
        final Map<T, Set<T>> succWithUnmodifiableSets = new HashMap<>();
        final Map<T, Set<T>> predWithUnmodifiableSets = new HashMap<>();
        final Set<T> modifiableRoots = new HashSet<>();
        for (final T node : nodes) {
            succWithUnmodifiableSets.put(node, Collections.unmodifiableSet(succ.get(node)));
            final Set<T> preds = pred.get(node);
            predWithUnmodifiableSets.put(node, Collections.unmodifiableSet(preds));
            if (preds.isEmpty()) {
                modifiableRoots.add(node);
            }
        }
        this.immediateSuccessors = Collections.unmodifiableMap(succWithUnmodifiableSets);
        this.roots = Collections.unmodifiableSet(modifiableRoots);
        this.cycles = findCycles(roots);
        final Map<T, Set<Path<T>>> modifiableCyclesPerNode = new HashMap<>();
        for (final Path<T> cycle : cycles) {
            for (final T cycleNode : cycle) {
                Util.addToValueSet(modifiableCyclesPerNode, cycleNode, cycle);
            }
        }
        cyclesPerNode = Collections.unmodifiableMap(modifiableCyclesPerNode);
        cycleClusters = computeCycleClusters();
    }

    private Set<Path<T>> findCycles(Set<T> roots) {
        final Set<Path<T>> cycles = new HashSet<>();
        for (final T root : roots) {
            Util.addAll(findCycles(new PathImpl<T>(Collections.singleton(root))), cycles);
        }
        return cycles;
    }

    private Iterable<Path<T>> findCycles(Path<T> path) {
        assert !path.isEmpty();
        final Set<Path<T>> result = new HashSet<>();
        final Set<T> successors = immediateSuccessors.get(path.tail());
        for (final T successor : successors) {
            Util.addAll(findCycles(path, successor), result);
        }
        return result;
    }

    private Iterable<Path<T>> findCycles(Path<T> path, T successor) {
        final Iterable<Path<T>> result;
        if (path.contains(successor)) {
            result = Collections.singleton(path.subPath(successor).extend(successor));
        } else {
            result = findCycles(path.extend(successor));
        }
        return result;
    }

    @Override
    public Set<T> getNodes() {
        return nodes;
    }

    @Override
    public Set<DirectedEdge<T>> getEdges() {
        return edges;
    }

    @Override
    public Set<T> getRoots() {
        return roots;
    }

    @Override
    public Set<Path<T>> getCycles() {
        return cycles;
    }
    
    @Override
    public CycleClusters<T> getCycleClusters() {
        return cycleClusters;
    }

    @Override
    public boolean areOnSameCycleCluster(T a, T b) {
        final CycleCluster<T> aCluster = cycleClusters.getCluster(a);
        final CycleCluster<T> bCluster = cycleClusters.getCluster(b);
        return aCluster != null && aCluster == bCluster;
    }

    @Override
    public boolean hasPath(T from, T to) {
        return hasPath(from, to, /* visited */ new HashSet<>());
    }
    
    private boolean hasPath(T from, T to, Set<T> visited) {
        final boolean result;
        if (from.equals(to)) {
            result = true;
        } else if (visited.contains(from)) {
            result = false;
        } else {
            Set<T> nextVisited = new HashSet<>(visited);
            nextVisited.add(from);
            result = immediateSuccessors.get(from).stream().anyMatch(successor->hasPath(successor, to, nextVisited));
        }
        return result;
    }

    @Override
    public Map<T, Integer> getLengthsOfLongestPathsFromRoot() {
        // construct a new graph with joined cycles replaced by combined "cycle nodes" and edges combined accordingly
        // then count in a breadth-first search starting from the roots
        Pair<DirectedGraph<T>, CycleClusters<T>> dag = graphWithCombinedCycleNodes();
        Map<T, Integer> preResult = dag.getA().getLengthsOfLongestPathsFromRootForDag();
        // now map back the results of the cycle representatives to all nodes that were part of the cycle:
        Map<T, Integer> result = new HashMap<>(preResult);
        for (final CycleCluster<T> cycleCluster : dag.getB().getClusters()) {
            final Integer rootDistance = result.get(cycleCluster.getRepresentative());
            for (final T representedCycleNode : cycleCluster.getClusterNodes()) {
                result.put(representedCycleNode, rootDistance);
            }
        }
        return result;
    }

    @Override
    public Pair<DirectedGraph<T>, CycleClusters<T>> graphWithCombinedCycleNodes() {
        assert cycleClusters != null;
        final Set<T> newNodes = new HashSet<>(nodes);
        newNodes.removeAll(cyclesPerNode.keySet()); // first remove all cycle nodes
        for (final CycleCluster<T> cluster : cycleClusters.getClusters()) {
            newNodes.add(cluster.getRepresentative()); // add the representative nodes
        }
        final Set<DirectedEdge<T>> newEdges = new HashSet<>();
        for (final DirectedEdge<T> edge : edges) {
            if (!isPartOfCycle(edge)) {
                newEdges.add(replaceCycleNodesByRepresentatives(edge, cycleClusters));
            }
        }
        return new Pair<>(new DirectedGraphImpl<T>(newNodes, newEdges), cycleClusters);
    }

    private CycleClusters<T> computeCycleClusters() {
        assert cycles != null;
        // keep disjoint sets of nodes such that each set consists of the union of one or more cycles
        final Set<Set<T>> combinedCycleNodes = new HashSet<>();
        for (final Path<T> cycle : cycles) {
            // see if cycle has a non-empty intersection with any of the already existing node sets:
            Set<T> firstIntersectionWith = null;
            Set<Set<T>> nodeSetsToRemoveAfterJoining = new HashSet<>();
            for (final Set<T> nodeSet : combinedCycleNodes) {
                if (intersects(cycle, nodeSet)) {
                    if (firstIntersectionWith == null) {
                        Util.addAll(cycle, nodeSet);
                        firstIntersectionWith = nodeSet;
                    } else {
                        // this is not the first node set that cycle intersects with; this
                        // requires joining the nodeSet with the firstIntersectionWith set:
                        firstIntersectionWith.addAll(nodeSet);
                        nodeSetsToRemoveAfterJoining.add(nodeSet);
                    }
                }
            }
            if (firstIntersectionWith == null) {
                final HashSet<T> newCombinedCycleNodes = new HashSet<>();
                Util.addAll(cycle, newCombinedCycleNodes);
                combinedCycleNodes.add(newCombinedCycleNodes);
            }
            combinedCycleNodes.removeAll(nodeSetsToRemoveAfterJoining);
        }
        final Set<CycleCluster<T>> representativeToCycleNodesItRepresents = new HashSet<>();
        for (final Set<T> cluster : combinedCycleNodes) {
            representativeToCycleNodesItRepresents.add(new CycleClusterImpl<>(cluster.iterator().next(), cluster));
        }
        return new CycleClustersImpl<>(representativeToCycleNodesItRepresents);
    }

    private DirectedEdge<T> replaceCycleNodesByRepresentatives(DirectedEdge<T> edge, CycleClusters<T> cycleClusters) {
        boolean replaced = false;
        T from = null;
        T to = null;
        final CycleCluster<T> fromCluster;
        final CycleCluster<T> toCluster;
        if (from == null && (fromCluster=cycleClusters.getCluster(edge.getFrom())) != null && !fromCluster.getRepresentative().equals(edge.getFrom())) {
            from = fromCluster.getRepresentative();
            replaced = true;
        } else {
            from = edge.getFrom();
        }
        if (from == null && (toCluster=cycleClusters.getCluster(edge.getTo())) != null && !toCluster.getRepresentative().equals(edge.getTo())) {
            to = toCluster.getRepresentative();
            replaced = true;
        } else {
            to = edge.getTo();
        }
        return replaced ? new DirectedEdgeImpl<>(from, to) : edge;
    }

    private boolean isPartOfCycle(DirectedEdge<T> edge) {
        return cycles.stream().anyMatch(cycle->cycle.contains(edge));
    }

    private boolean intersects(Path<T> cycle, Set<T> nodeSet) {
        return nodeSet.stream().anyMatch(node->cycle.contains(node));
    }

    @Override
    public Map<T, Integer> getLengthsOfLongestPathsFromRootForDag() {
        if (!cycles.isEmpty()) {
            throw new IllegalStateException("Can't call this method on a graph with cycles; use graphWithCombinedCycleNodes() first");
        }
        final Map<T, Integer> result = new HashMap<>();
        Set<T> workingSet = new HashSet<>(roots);
        int longestDistanceFromRoot = 0;
        while (!workingSet.isEmpty()) {
            final Set<T> nextWorkingSet = new HashSet<>();
            for (final T node : workingSet) {
                if (!result.containsKey(node) || result.get(node) < longestDistanceFromRoot) {
                    result.put(node, longestDistanceFromRoot);
                    nextWorkingSet.addAll(immediateSuccessors.get(node));
                }
            }
            longestDistanceFromRoot++;
            workingSet = nextWorkingSet;
        }
        return result;
    }

}
