package com.sap.sse.common;

import java.util.AbstractList;
import java.util.List;

public class ReverseRandomAccessList<E> extends AbstractList<E> implements List<E> {
    private final List<E> forwardList;
    
    public ReverseRandomAccessList(List<E> forwardList) {
        this.forwardList = forwardList;
    }
    
    @Override
    public E get(int index) {
        return forwardList.get(forwardList.size()-1-index);
    }

    @Override
    public int size() {
        return forwardList.size();
    }

}
