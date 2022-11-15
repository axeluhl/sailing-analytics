package com.sap.sailing.windestimation.aggregator.graph;

/**
 * A node in a {@link Tree}. If it has no (a {@code null}) {@link #getParent() parent}, it is the {@link Tree#getRoot()
 * root} of the tree. If it has an empty set of {@link #getChildren() children}, it is called a <em>leaf</em> in the tree.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <TN>
 */
public interface TreeNode<TN extends TreeNode<TN>> {
    /**
     * @return {@code null} if this is the {@link Tree}'s root node; the parent node in whose {@link #getChildren()
     *         children collection} this node is contained
     */
    TN getParent();
    
    /**
     * The children of this node in the {@link Tree}. May be empty for leaf nodes, but always returns a valid
     * {@link Iterable}.
     */
    Iterable<TN> getChildren();
}
