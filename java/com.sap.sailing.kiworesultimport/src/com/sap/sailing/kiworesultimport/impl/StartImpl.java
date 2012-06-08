package com.sap.sailing.kiworesultimport.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.w3c.dom.Node;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.kiworesultimport.Start;

public class StartImpl extends AbstractNodeWrapper implements Start {
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-ddX");

    public StartImpl(Node node) {
        super(node);
    }

    @Override
    public String getBootsklasse() {
        return getNode().getAttributes().getNamedItem("bootsklasse").getNodeValue();
    }

    @Override
    public Integer getWettfahrt() {
        return Integer.valueOf(getNode().getAttributes().getNamedItem("wettfahrt").getNodeValue());
    }

    @Override
    public String getStartgruppe() {
        return getNode().getAttributes().getNamedItem("startgruppe").getNodeValue();
    }

    @Override
    public String getKurs() {
        return getNode().getAttributes().getNamedItem("kurs").getNodeValue();
    }

    @Override
    public String getStartzeit() {
        return getNode().getAttributes().getNamedItem("startzeit").getNodeValue();
    }

    @Override
    public TimePoint getTimePoint() throws ParseException {
        return new MillisecondsTimePoint(df.parse(getStartzeit()));
    }

    @Override
    public String getStartflagge() {
        return getNode().getAttributes().getNamedItem("startflagge").getNodeValue();
    }

    @Override
    public String getBemerkung() {
        return getNode().getAttributes().getNamedItem("bemerkung").getNodeValue();
    }

    @Override
    public Boolean getDoppelteWertung() {
        return Boolean.valueOf(getNode().getAttributes().getNamedItem("doppelte_Wertung").getNodeValue());
    }

    @Override
    public Boolean getStreichbar() {
        return Boolean.valueOf(getNode().getAttributes().getNamedItem("streichbar").getNodeValue());
    }

}
