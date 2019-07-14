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
    private final Function<String, T> artificialInnerNodeConstructor;
    
    /**
     * Constructs the inner graph's edge structure from the {@code overarchingTree}.
     * 
     * @param overarchingTree
     *            defines the tree of nodes of type {@link GroupOutOfWhichToPickTheBestElement} or a subtype thereof;
     *            the inner {@link GroupOutOfWhichToPickTheBestElement#getElements() elements} of each of this tree's
     *            nodes is used as a node of the "inner graph," and edges are defined by this new object leading from
     *            all of the {@code overarchingTree}'s child inner
     *            {@link GroupOutOfWhichToPickTheBestElement#getElements() elements} to each of those child's parent's
     *            inner {@link GroupOutOfWhichToPickTheBestElement#getElements() elements}.
     * 
     * @param artificialInnerNodeConstructor
     *            all inner {@link GroupOutOfWhichToPickTheBestElement#getElements() elements} of the
     *            {@code overarchingTree}'s root node have to be connected to a single "end node" that needs to be
     *            constructed. Likewise, all leaf nodes of the {@code overarchingTree} need an artificial start node
     *            that has all of the leaf node's {@link GroupOutOfWhichToPickTheBestElement#getElements() elements} as
     *            its successors. This node constructor is used to construct those artificial inner graph start and end
     *            nodes. It accepts a {@link String} as a name argument. The {@link ElementWithQuality#getQuality()
     *            quality} of the element created is expected to be set to the same value for all invocations, e.g.,
     *            {@code 1.0}.
     */
    public InnerGraphSuccessorSupplier(Tree<G> overarchingTree, Function<String, T> artificialInnerNodeConstructor) {
        successors = new HashMap<>();
        this.artificialInnerNodeConstructor = artificialInnerNodeConstructor;
        artificialRoot = artificialInnerNodeConstructor.apply("End Node at Root");
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
            final T artificialLeaf = artificialInnerNodeConstructor.apply("Start Node for "+node);
            artificialLeaves.put(node, artificialLeaf);
            innerChildElements = Collections.singleton(artificialLeaf);
        } else {
            final Set<T> allInnerChildNodes = new HashSet<>();
            for (final G child : node.getChildren()) {
                for (final T innerChildElement : child.getElements()) {
                    allInnerChildNodes.add(innerChildElement);
                }
                addAllEdges(child);
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

    /**
     * @return the artificial single root node that is parent of all of the
     *         {@link GroupOutOfWhichToPickTheBestElement#getElements() elements} of the {@link Tree#getRoot() root} of
     *         the {@link Tree} passed to this object's constructor.
     */
    public T getArtificialRoot() {
        return artificialRoot;
    }
    
    /**
     * @return the artificial leaf node for which all {@link GroupOutOfWhichToPickTheBestElement#getElements() elements}
     *         of the {@code treeLeafNode} are configured as successors.
     */
    public T getArtificialLeaf(G treeLeafNode) {
        return artificialLeaves.get(treeLeafNode);
    }
}
