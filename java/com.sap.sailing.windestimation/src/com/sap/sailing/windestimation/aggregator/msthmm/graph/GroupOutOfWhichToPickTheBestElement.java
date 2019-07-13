package com.sap.sailing.windestimation.aggregator.msthmm.graph;

/**
 * A special {@link TreeNode tree node} that has a number of elements, each of which representing a node in a graph to
 * which the {@link DijsktraShortestPathFinder} can be applied. This "inner graph" has a structure that aligns with the
 * path from a leaf in the {@link Tree} up to the {@link Tree#getRoot() root} of that tree. It traverses the
 * {@link Tree}'s levels. At each level (at each {@link TreeNode} along the path to the root node) there are a number of
 * {@link #getElements() elements}. The inner graph has edges starting at each {@link #getElements() element} and
 * pointing to each of the {@link #getParent() parent's} {@link #getElements() elements}. The {@link ElementAdjacencyQualityMetric}
 * describes the quality (or "probability") of each such edge. See also {@link InnerGraphSuccessorSupplier}.<p>
 * 
 * The interesting spots in the overarching {@link Tree} are those objects of this type that have more than one
 * {@link #getChildren() child}. For those, two "inner graphs" from a leaf to the root of the overarching {@link Tree}
 * meet and then follow the same path up to the root. When it comes to picking the best element in such a group, optimizing
 * along each path from the leaf to the root may come to different solutions for this node. Such nodes then need to be
 * disambiguated in some way and will violate one of the two "inner graphs'" computed optimum.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 */
public interface GroupOutOfWhichToPickTheBestElement<T extends ElementWithQuality, G extends GroupOutOfWhichToPickTheBestElement<T, G>>
extends TreeNode<G> {
    /**
     * Obtains the elements in this group. Exactly one of them has to be selected, based on the element's
     * {@link ElementWithQuality#getQuality() quality} as well as the edges connecting elements of this type with each
     * other.
     */
    Iterable<T> getElements();
}
