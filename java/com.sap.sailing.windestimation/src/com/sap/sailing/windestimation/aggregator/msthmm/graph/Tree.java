package com.sap.sailing.windestimation.aggregator.msthmm.graph;

/**
 * An acyclic tree of {@link TreeNode nodes} with a single root node.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <TN>
 */
public interface Tree<TN extends TreeNode<TN>> {
    TreeNode<TN> getRoot();
}
