package com.sap.sse.util.graph;

import java.util.Map;
import java.util.Set;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.util.graph.impl.DirectedGraphImpl;

public interface DirectedGraph<T> {
    static <T> DirectedGraph<T> create(Set<T> nodes, Set<DirectedEdge<T>> edges) {
        return new DirectedGraphImpl<T>(nodes, edges);
    }
    
    Set<T> getNodes();
    Set<DirectedEdge<T>> getEdges();
    Set<T> getRoots();
    
    /**
     * When cycles overlap, they are combined into {@link CycleCluster}s, each being
     * {@link CycleCluster#getRepresentative() represented} by a dedicated node from that cluster.
     * The cycle analysis on this graph can tell the individual {@link #getCycles() cycles}, whereas
     * this method tells the combination of cycles into clusters based on their overlap. The result
     * is also called "strongly-connected components."
     */
    CycleClusters<T> getCycleClusters();

    /**
     * Returns {@code true} if and only if {@link #getCycleClusters()} has a cluster that
     * contains both, {@code a} and {@code b}. Note that a node is either in one or in no
     * cycle cluster. It cannot be in two cycle clusters at the same time because then those
     * two clusters would have an overlap which by their definition would have required them
     * to get joined into one.
     */
    boolean areOnSameCycleCluster(T a, T b);
    
    /**
     * Tries to find any path leading from {@code from} to {@code to}. If no such
     * path can be found, {@code null} is returned.
     */
    boolean hasPath(T from, T to);
    
    /**
     * Tells in the values how long the longest path from any node in {@link #getRoots()} to the key node is. If the key
     * is {@link Set#contains(Object) contained} in {@link #getRoots()} then the value is {@code 0}. For nodes in any
     * cyclic path returned by {@link #getCycles()} the cycle is treated as if it were a single node, with the edges
     * forming the cycle removed, and with edges to/from any of the cycle nodes replaced by corresponding edges to/from
     * the combined "cycle node." All cycle nodes then receive the value that the combined "cycle node" receives.
     */
    Map<T, Integer> getLengthsOfLongestPathsFromRoot();
    
    /**
     * Implements {@link #getLengthsOfLongestPathsFromRoot()} but excludes the possibility of cycles. It can
     * therefore be applied to what the {@link #graphWithCombinedCycleNodes()} method returns;
     */
    Map<T, Integer> getLengthsOfLongestPathsFromRootForDag();
    
    /**
     * Produces a new graph based on this graph where all nodes that are part of one or more cycles are
     * combined into a single node, all edges within the cycles removed, and all edges leading into/out of a
     * cycle replaced by a corresponding edge leading to/from the combined cycle node. The resulting graph
     * is a directed acyclic graph (DAG) with {@link #getCycles()}.{@link Set#isEmpty() isEmpty()}{@code == true}.
     * The second element of the pair returned describes the replacements carried out: keys are the representatives
     * of one or more joined cycles, values are the cycle nodes collectively represented by the key node.
     */
    Pair<DirectedGraph<T>, CycleClusters<T>> graphWithCombinedCycleNodes();

    CycleCluster<T> getCycleCluster(T node);
    
}
