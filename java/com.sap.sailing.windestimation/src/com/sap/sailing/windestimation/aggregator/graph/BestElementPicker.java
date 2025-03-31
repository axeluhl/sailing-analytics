package com.sap.sailing.windestimation.aggregator.graph;

/**
 * An algorithm that, given a tree of element groups, from each group picks an element considered "best" according to
 * two metrics: the {@link ElementWithQuality#getQuality() quality} of the element itself, and the
 * {@link ElementAdjacencyQualityMetric} that evaluates the quality of the neighborhood of two elements in the
 * {@link Tree}.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 */
public interface BestElementPicker<T extends ElementWithQuality, G extends GroupOutOfWhichToPickTheBestElement<T, G>> {
    Iterable<T> getBestElements(Tree<GroupOutOfWhichToPickTheBestElement<T, G>> tree);
}
