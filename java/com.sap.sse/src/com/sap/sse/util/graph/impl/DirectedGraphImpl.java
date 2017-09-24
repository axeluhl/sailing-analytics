package com.sap.sse.util.graph.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    
    private final Iterable<Path<T>> cycles;
    
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
        this.cycles = findCycles();
        final Map<T, Set<Path<T>>> modifiableCyclesPerNode = new HashMap<>();
        for (final Path<T> cycle : cycles) {
            for (final T cycleNode : cycle) {
                Util.addToValueSet(modifiableCyclesPerNode, cycleNode, cycle);
            }
        }
        cyclesPerNode = Collections.unmodifiableMap(modifiableCyclesPerNode);
        cycleClusters = computeCycleClusters();
        assert cycleClusters.areDisjoint();
    }

    private Iterable<Path<T>> findCycles() {
        final Set<T> nodesNotVisited = new HashSet<>(nodes);
        final Set<Path<T>> result = new HashSet<>(); // the cycles found
        List<Path<T>> worklist = roots.stream().map(r->new PathImpl<T>(Collections.singleton(r))).collect(Collectors.toList());
        if (Util.isEmpty(worklist) && !nodesNotVisited.isEmpty()) {
            // all nodes seem to be on cycles; no root found; pick any node to start with:
            final T nextNonRootLikelyInCycle = nodesNotVisited.iterator().next();
            nodesNotVisited.remove(nextNonRootLikelyInCycle);
            worklist = new ArrayList<>();
            worklist.add(new PathImpl<>(Collections.singleton(nextNonRootLikelyInCycle)));
        }
        final Set<T> visited = new HashSet<>();
        while (!Util.isEmpty(worklist)) {
            // depth-first search by using last element in worklist and replacing it by paths extended by its successors
            final Path<T> p = worklist.remove(worklist.size()-1);
            if (!visited.contains(p.tail())) {
                visited.add(p.tail());
                nodesNotVisited.remove(p.tail());
                // at this point, should the index drop to the current size the next time and no cycle was added since now, p.tail() has been proven to not be part of a cycle because all outgoing paths were followed;
                final Set<T> successors = immediateSuccessors.get(p.tail());
                for (final T successor : successors) {
                    if (p.contains(successor)) {
                        // cycle found
                        result.add(p.subPath(successor).extend(successor));
                    } else {
                        worklist.add(p.extend(successor));
                    }
                }
            }
            if (worklist.isEmpty() && !nodesNotVisited.isEmpty()) {
                // there are nodes remaining which must be on cycles because they
                // haven't been reached from any of the root nodes. Add path to first
                // node not yet visited and continue:
                final T nextNonRootLikelyInCycle = nodesNotVisited.iterator().next();
                nodesNotVisited.remove(nextNonRootLikelyInCycle);
                worklist.add(new PathImpl<>(Collections.singleton(nextNonRootLikelyInCycle)));
            }
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
    public Iterable<Path<T>> getCycles() {
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
        Set<Path<T>> workingSet = Collections.singleton(new PathImpl<>(Arrays.asList(from)));
        while (!workingSet.isEmpty()) {
            final Set<Path<T>> nextWorkingSet = new HashSet<>();
            for (final Path<T> p : workingSet) {
                if (p.tail().equals(to)) {
                    return true;
                }
                final Set<T> successors = immediateSuccessors.get(p.tail());
                for (final T successor : successors) {
                    if (!p.contains(successor)) {
                        // no cycle found; continue following that path
                        nextWorkingSet.add(p.extend(successor));
                    }
                }
            }
            workingSet = nextWorkingSet;
        }
        return false;
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
            if (!cycleClusters.isEdgeInCycleCluster(edge)) {
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
        if (to == null && (toCluster=cycleClusters.getCluster(edge.getTo())) != null && !toCluster.getRepresentative().equals(edge.getTo())) {
            to = toCluster.getRepresentative();
            replaced = true;
        } else {
            to = edge.getTo();
        }
        return replaced ? new DirectedEdgeImpl<>(from, to) : edge;
    }

    private boolean intersects(Path<T> cycle, Set<T> nodeSet) {
        return nodeSet.stream().anyMatch(node->cycle.contains(node));
    }

    @Override
    public Map<T, Integer> getLengthsOfLongestPathsFromRootForDag() {
        if (!Util.isEmpty(cycles)) {
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
