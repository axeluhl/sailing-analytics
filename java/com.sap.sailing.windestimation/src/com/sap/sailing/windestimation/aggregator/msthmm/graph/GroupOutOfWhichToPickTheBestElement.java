package com.sap.sailing.windestimation.aggregator.msthmm.graph;

public interface GroupOutOfWhichToPickTheBestElement<T extends ElementWithQuality> extends TreeNode<GroupOutOfWhichToPickTheBestElement<T>> {
    /**
     * Obtains the elements in this group. Exactly one of them has to be selected, based on the element's
     * {@link ElementWithQuality#getQuality() quality} as well as the edges connecting elements of this type with each
     * other.
     */
    Iterable<T> getElements();
}
