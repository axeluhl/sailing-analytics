package com.sap.sailing.windestimation.aggregator.graph;

public interface ElementWithQuality {
    /**
     * @return a "quality" metric; the higher the better; one of two inputs to pick a good element. The other input is a
     *         graph with edges connecting elements where the edges have a quality, too.
     */
    double getQuality();
}
