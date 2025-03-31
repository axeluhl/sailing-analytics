package com.sap.sse.util.graph;

import java.util.Set;

/**
 * A cluster of nodes with one or more cycles and a selection of one node from the
 * cluster that serves as a representative of the cluster. Can be used in replacing
 * the cluster by the representative node, thus forming a directed acyclic graph.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CycleCluster<T> {
    T getRepresentative();
    Set<T> getClusterNodes();
    boolean contains(T node);
}
