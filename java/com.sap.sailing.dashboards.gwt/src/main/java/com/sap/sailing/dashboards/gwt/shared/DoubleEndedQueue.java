package com.sap.sailing.dashboards.gwt.shared;

import java.util.ArrayList;
import java.util.List;

public class DoubleEndedQueue {

    private List<Double> deque = new ArrayList<Double>();

    public void insertFront(double item) {
        deque.add(0, item);
    }

    public void insertRear(double item) {
        deque.add(item);
    }

    public double removeFront() {
        if (deque.isEmpty()) {
            return 0;
        }
        double rem = deque.remove(0);
        return rem;
    }

    public void removeRear() {
        if (deque.isEmpty()) {
            return;
        }
    }

    public double peakFront() {
        double item = deque.get(0);
        return item;
    }

    public double peakRear() {
        double item = deque.get(deque.size() - 1);
        return item;
    }

    public int getSize() {
        return deque.size();
    }
}
