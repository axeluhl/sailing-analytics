package com.sap.sailing.windestimation.aggregator.msthmm.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.sap.sse.common.Util;

/**
 * A "successor supplier" constructing edges of a graph that lies within a {@link Tree}, ascending upwards from the
 * {@link Tree}'s leaves to the {@link Tree}'s {@link Tree#getRoot() root} node. The tree is assumed to consist of nodes
 * of some subtype of {@link GroupOutOfWhichToPickTheBestElement}, so each {@link TreeNode} has a number of
 * {@link GroupOutOfWhichToPickTheBestElement#getElements() elements. Starting at an artificial start node constructed
 * for each of the {@link Tree}'s leaves, an edge leads to each of the
 * {@link GroupOutOfWhichToPickTheBestElement#getElements() elements} of the leaf node. From each of those, an edge
 * leads to each of the {@link GroupOutOfWhichToPickTheBestElement#getElements() elements} of the
 * {@link TreeNode#getParent() parent} node in the overarching tree, and so on, up to the {@link Tree}'s
 * {@link Tree#getRoot() root}. All {@link GroupOutOfWhichToPickTheBestElement#getElements() elements} of the
 * root node get an additional artifical edge to an artificial root node of the "inner graph."
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 * @param <G>
 */
public class InnerGraphSuccessorSupplier<T extends ElementWithQuality, G extends GroupOutOfWhichToPickTheBestElement<T, G>>
implements Function<T, Iterable<T>> {
    private final Map<T, Set<T>> successors;
    private final T artificialRoot;
    private final Map<G, T> artificialLeaves;
    
    public InnerGraphSuccessorSupplier(Tree<G> overarchingTree) {
        successors = new HashMap<>();
        artificialRoot = null; // TODO generate an artificial root of type T
        artificialLeaves = new HashMap<>();
        if (overarchingTree.getRoot() != null) {
            // add the edges to the artificial root node
            for (final T innerRootElement : overarchingTree.getRoot().getElements()) {
                Util.addToValueSet(successors, innerRootElement, artificialRoot);
            }
            addAllEdges(overarchingTree.getRoot());
        }
    }
    
    private void addAllEdges(G node) {
        final Iterable<T> innerChildElements;
        if (Util.isEmpty(node.getChildren())) {
            // leaf node; add an artificial leaf:
            final T artificialLeaf = null; // TODO generate an artificial leaf of type T
            artificialLeaves.put(node, artificialLeaf);
            innerChildElements = Collections.singleton(artificialLeaf);
        } else {
            final Set<T> allInnerChildNodes = new HashSet<>();
            for (final GroupOutOfWhichToPickTheBestElement<T, G> child : node.getChildren()) {
                for (final T innerChildElement : child.getElements()) {
                    allInnerChildNodes.add(innerChildElement);
                }
            }
            innerChildElements = allInnerChildNodes;
        }
        for (final T innerChildElement : innerChildElements) {
            for (final T innerParentElement : node.getElements()) {
                Util.addToValueSet(successors, innerChildElement, innerParentElement);
            }
        }
    }

    @Override
    public Iterable<T> apply(T t) {
        return successors.get(t);
    }
}
