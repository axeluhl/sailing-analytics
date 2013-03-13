package com.sap.sailing.odf.resultimport.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.swisstimingadapter.DomainFactory;
import com.sap.sailing.odf.resultimport.Competition;
import com.sap.sailing.odf.resultimport.OdfBody;

public class OdfBodyImpl extends AbstractNodeWrapper implements OdfBody {
    private static final Logger logger = Logger.getLogger(OdfBodyImpl.class.getName());
    
    private final DomainFactory swissTimingDomainFactory;
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    public OdfBodyImpl(Node node, DomainFactory swissTimingDomainFactory) {
        super(node);
        this.swissTimingDomainFactory = swissTimingDomainFactory;
    }

    @Override
    public Iterable<Competition> getCompetitions() {
        List<Competition> result = new ArrayList<>();
        Element element = (Element) getNode();
        NodeList cumulativeResults = element.getElementsByTagName("Competition");
        for (int i=0; i<cumulativeResults.getLength(); i++) {
            result.add(new CompetitionImpl(cumulativeResults.item(i)));
        }
        return result;
    }

    @Override
    public String getDocumentSubtype() {
        return getNode().getAttributes().getNamedItem("DocumentSubtype").getNodeValue();
    }

    @Override
    public String getResultStatus() {
        return getNode().getAttributes().getNamedItem("ResultStatus").getNodeValue();
    }

    @Override
    public String getVersion() {
        return getNode().getAttributes().getNamedItem("Version").getNodeValue();
    }

    @Override
    public String getFeedFlag() {
        return getNode().getAttributes().getNamedItem("FeedFlag").getNodeValue();
    }

    @Override
    public TimePoint getTimePoint() {
        TimePoint result;
        String timeString = getNode().getAttributes().getNamedItem("Time").getNodeValue();
        String dateString = getNode().getAttributes().getNamedItem("Date").getNodeValue();
        try {
            result = new MillisecondsTimePoint(dateFormat.parse(dateString+timeString));
        } catch (ParseException e) {
            logger.throwing(OdfBodyImpl.class.getName(), "getTimePoint", e);
            result = null;
        }
        return result;
    }

    @Override
    public String getVenue() {
        return getNode().getAttributes().getNamedItem("Venue").getNodeValue();
    }

    @Override
    public String getEventName() {
        return getDocumentSubtype()+"@"+getVenue();
    }

    @Override
    public String getBoatClassName() {
        return swissTimingDomainFactory.getOrCreateBoatClassFromRaceID(getDocumentSubtype()).getName();
    }

    @Override
    public String toString() {
        return "OdfBody for event "+getEventName()+", result status "+getResultStatus()+
                "@"+getTimePoint()+", version "+getVersion()+" for boat class "+getBoatClassName()+
                ". Competitions: "+getCompetitions();
    }
}
