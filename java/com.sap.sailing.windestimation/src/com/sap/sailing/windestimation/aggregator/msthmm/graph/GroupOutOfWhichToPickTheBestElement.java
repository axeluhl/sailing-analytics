package com.sap.sailing.windestimation.aggregator.msthmm.graph;

/**
 * A special {@link TreeNode tree node} that 
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 */
public interface GroupOutOfWhichToPickTheBestElement<T extends ElementWithQuality> extends TreeNode<GroupOutOfWhichToPickTheBestElement<T>> {
    /**
     * Obtains the elements in this group. Exactly one of them has to be selected, based on the element's
     * {@link ElementWithQuality#getQuality() quality} as well as the edges connecting elements of this type with each
     * other.
     */
    Iterable<T> getElements();
}
