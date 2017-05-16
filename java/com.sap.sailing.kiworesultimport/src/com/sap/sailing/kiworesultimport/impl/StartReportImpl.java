package com.sap.sailing.kiworesultimport.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.sailing.kiworesultimport.Start;
import com.sap.sailing.kiworesultimport.StartReport;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class StartReportImpl extends AbstractNodeWrapper implements StartReport {
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-ddX");

    private final String sourceName;
    
    public StartReportImpl(Node node, String sourceName) {
        super(node);
        this.sourceName = sourceName;
    }

    @Override
    public String getSourceName() {
        return sourceName;
    }
    
    @Override
    public String getDateAsString() {
        return ((Element) getNode()).getElementsByTagName("datum").item(0).getNodeValue();
    }

    @Override
    public TimePoint getTimePoint() throws ParseException {
        String datum = getDateAsString();
        return datum == null || datum.trim().length() == 0 ? null : new MillisecondsTimePoint(df.parse(datum));
    }

    @Override
    public String getCourseAreaName() {
        return ((Element) getNode()).getElementsByTagName("regattabahn").item(0).getNodeValue();
    }

    @Override
    public String getStartBearingAsString() {
        return ((Element) getNode()).getElementsByTagName("kompasskurs").item(0).getNodeValue();
    }

    @Override
    public String getWindSpeedAsString() {
        return ((Element) getNode()).getElementsByTagName("windstaerke").item(0).getNodeValue();
    }

    @Override
    public String getWindDirectionAsString() {
        return ((Element) getNode()).getElementsByTagName("windrichtung").item(0).getNodeValue();
    }

    @Override
    public Iterable<Start> getStarts() {
        List<Start> result = new ArrayList<Start>();
        final NodeList starts = ((Element) getNode()).getElementsByTagName("start");
        for (int i=0; i<starts.getLength(); i++) {
            final Element startNode = (Element) starts.item(i);
            final StartImpl start = new StartImpl(startNode);
            if (start.getBoatClass() != null) {
                result.add(start);
            }
        }
        return result;
    }

}
