package com.sap.sailing.kiworesultimport.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.kiworesultimport.Start;
import com.sap.sailing.kiworesultimport.Startbericht;

public class StartberichtImpl extends AbstractNodeWrapper implements Startbericht {
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-ddX");

    public StartberichtImpl(Node node) {
        super(node);
    }

    @Override
    public String getDatum() {
        return getNode().getAttributes().getNamedItem("datum").getNodeValue();
    }

    @Override
    public TimePoint getTimePoint() throws ParseException {
        String datum = getDatum();
        return datum == null || datum.trim().length() == 0 ? null : new MillisecondsTimePoint(df.parse(datum));
    }

    @Override
    public String getRegattabahn() {
        return getNode().getAttributes().getNamedItem("regattabahn").getNodeValue();
    }

    @Override
    public String getKompasskurs() {
        return getNode().getAttributes().getNamedItem("kompasskurs").getNodeValue();
    }

    @Override
    public String getWindstaerke() {
        return getNode().getAttributes().getNamedItem("windstaerke").getNodeValue();
    }

    @Override
    public String getWindrichtung() {
        return getNode().getAttributes().getNamedItem("windrichtung").getNodeValue();
    }

    @Override
    public Iterable<Start> getStarts() {
        List<Start> result = new ArrayList<Start>();
        final NodeList starts = ((Element) getNode()).getElementsByTagName("start");
        for (int i=0; i<starts.getLength(); i++) {
            result.add(new StartImpl(starts.item(i)));
        }
        return result;
    }

}
