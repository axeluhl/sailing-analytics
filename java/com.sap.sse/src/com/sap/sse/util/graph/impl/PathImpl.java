package com.sap.sse.util.graph.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.sap.sse.common.Util;
import com.sap.sse.util.graph.DirectedEdge;
import com.sap.sse.util.graph.Path;

public class PathImpl<T> implements Path<T> {
    private final Set<T> nodesForFastContains;
    private final List<T> nodesInOrder;
    
    public PathImpl(Iterable<T> nodes) {
        this.nodesForFastContains = new HashSet<>();
        this.nodesInOrder = new ArrayList<>(Util.size(nodes));
        Util.addAll(nodes, this.nodesForFastContains);
        Util.addAll(nodes, this.nodesInOrder);
    }

    @Override
    public Iterable<T> getNodes() {
        return Collections.unmodifiableCollection(nodesInOrder);
    }
    
    @Override
    public Path<T> extend(T tail) {
        final List<T> newNodes = new ArrayList<>(nodesInOrder);
        newNodes.add(tail);
        return new PathImpl<T>(newNodes);
    }
    
    @Override
    public Path<T> extendAtHead(T head) {
        final List<T> newNodes = new ArrayList<>(nodesInOrder.size()+1);
        newNodes.add(head);
        newNodes.addAll(nodesInOrder);
        return new PathImpl<T>(newNodes);
    }
    
    @Override
    public Path<T> subPath(T start) {
        final List<T> newNodes = new ArrayList<>();
        boolean found = false;
        for (final T node : nodesInOrder) {
            found = found || node.equals(start);
            if (found) {
                newNodes.add(node);
            }
        }
        return new PathImpl<T>(newNodes);
    }

    @Override
    public Path<T> subPath(T start, T endExclusive) {
        final List<T> newNodes = new ArrayList<>();
        boolean found = false;
        for (final T node : nodesInOrder) {
            found = (found || node.equals(start)) && !node.equals(endExclusive);
            if (found) {
                newNodes.add(node);
            }
        }
        return new PathImpl<T>(newNodes);
    }

    @Override
    public boolean contains(T node) {
        return nodesForFastContains.contains(node);
    }

    @Override
    public boolean contains(DirectedEdge<T> edge) {
        final int indexOfFrom;
        return contains(edge.getFrom()) &&
                (indexOfFrom=nodesInOrder.indexOf(edge.getFrom())) != nodesInOrder.size()-1 &&
                nodesInOrder.get(indexOfFrom+1).equals(edge.getTo());
    }

    @Override
    public T head() {
        return nodesInOrder.isEmpty() ? null : nodesInOrder.get(0);
    }

    @Override
    public T tail() {
        return nodesInOrder.isEmpty() ? null : nodesInOrder.get(nodesInOrder.size()-1);
    }

    @Override
    public boolean isEmpty() {
        return nodesForFastContains.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return nodesInOrder.iterator();
    }

    @Override
    public boolean intersects(Path<T> other) {
        return StreamSupport.stream(other.spliterator(), /* parallel */ false).anyMatch(n->contains(n));
    }

    @Override
    public String toString() {
        return "("+nodesInOrder.stream().map(n->n.toString()).collect(Collectors.joining(" -> "))+")";
    }
}
