package com.sap.sailing.windestimation.aggregator.graph;

/**
 * An acyclic tree of {@link TreeNode nodes} with a single root node.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <TN>
 */
@FunctionalInterface
public interface Tree<TN extends TreeNode<? extends TN>> {
    TN getRoot();
}
