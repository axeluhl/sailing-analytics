package com.sap.sailing.kiworesultimport.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sap.sailing.kiworesultimport.Start;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class StartImpl extends AbstractNodeWrapper implements Start {
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-ddX");

    public StartImpl(Node node) {
        super(node);
    }

    @Override
    public String getBoatClass() {
        final Node item = ((Element) getNode()).getElementsByTagName("bootsklasse").item(0);
        return item == null ? null : item.getFirstChild().getNodeValue();
    }

    @Override
    public Integer getRaceNumber() {
        return Integer.valueOf(((Element) getNode()).getElementsByTagName("wettfahrt").item(0).getFirstChild().getNodeValue());
    }

    @Override
    public String getFleetName() {
        return ((Element) getNode()).getElementsByTagName("startgruppe").item(0).getFirstChild().getNodeValue();
    }

    @Override
    public String getCourseName() {
        return ((Element) getNode()).getElementsByTagName("kurs").item(0).getFirstChild().getNodeValue();
    }

    @Override
    public String getStartTimeAsString() {
        return ((Element) getNode()).getElementsByTagName("startzeit").item(0).getFirstChild().getNodeValue();
    }

    @Override
    public TimePoint getTimePoint() throws ParseException {
        return new MillisecondsTimePoint(df.parse(getStartTimeAsString()));
    }

    @Override
    public String getStartFlag() {
        return ((Element) getNode()).getElementsByTagName("startflagge").item(0).getFirstChild().getNodeValue();
    }

    @Override
    public String getComment() {
        return ((Element) getNode()).getElementsByTagName("bemerkung").item(0).getFirstChild().getNodeValue();
    }

    @Override
    public Boolean isDoubleScore() {
        return Boolean.valueOf(((Element) getNode()).getElementsByTagName("doppelte_Wertung").item(0).getFirstChild().getNodeValue());
    }

    @Override
    public Boolean isDiscardable() {
        return Boolean.valueOf(((Element) getNode()).getElementsByTagName("streichbar").item(0).getFirstChild().getNodeValue());
    }

}
