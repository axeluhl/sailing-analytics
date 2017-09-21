package com.sap.sse.util.graph;

import java.util.Map;
import java.util.Set;

import com.sap.sse.common.Util.Pair;

public interface DirectedGraph<T> {
    Set<T> getNodes();
    Set<DirectedEdge<T>> getEdges();
    Set<T> getRoots();
    Set<Path<T>> getCycles();
    
    /**
     * Returns {@code true} if and only if {@link #getCycles()} returns one or more paths such that
     * one path contains both, {@code a} and {@code b}.
     */
    boolean areOnSameCycle(T a, T b);
    
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
    Pair<DirectedGraph<T>, Map<T, Set<T>>> graphWithCombinedCycleNodes();
}
