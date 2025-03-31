package com.sap.sailing.kiworesultimport.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.sailing.kiworesultimport.Boat;
import com.sap.sailing.kiworesultimport.BoatResultInRace;
import com.sap.sailing.kiworesultimport.ResultList;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ResultListImpl extends AbstractNodeWrapper implements ResultList {
    private static final Logger logger = Logger.getLogger(ResultListImpl.class.getName());
    
    private final String sourceName;
    
    private static final SimpleDateFormat df = new SimpleDateFormat("dd. MMM yyyy HH:mm", Locale.GERMAN);
    
    public ResultListImpl(Node node, String sourceName) {
        super(node);
        this.sourceName = sourceName;
    }
    
    @Override
    public String getSourceName() {
        return sourceName;
    }

    @Override
    public String getLegend() {
        return getNode().getAttributes().getNamedItem("legende").getNodeValue();
    }

    @Override
    public String getImagePath() {
        return getNode().getAttributes().getNamedItem("imagePfad").getNodeValue();
    }

    @Override
    public String getStatus() {
        return getNode().getAttributes().getNamedItem("status").getNodeValue();
    }

    @Override
    public String getBoatClassName() {
        return getNode().getAttributes().getNamedItem("class").getNodeValue();
    }

    @Override
    public String getEvent() {
        return getNode().getAttributes().getNamedItem("event").getNodeValue();
    }

    @Override
    public String getTime() {
        return getNode().getAttributes().getNamedItem("time").getNodeValue();
    }

    @Override
    public String getDate() {
        return getNode().getAttributes().getNamedItem("date").getNodeValue();
    }

    @Override
    public TimePoint getTimePointPublished() {
        TimePoint result = null;
        String dateTime = getDate()+" "+getTime();
        try {
            Date d = df.parse(dateTime);
            result = new MillisecondsTimePoint(d);
        } catch (ParseException e) {
            logger.info("Failed to parse result list date/time "+dateTime);
        }
        return result;
    }

    @Override
    public Iterable<Boat> getBoats() {
        List<Boat> result = new ArrayList<Boat>();
        final NodeList verteilung = ((Element) getNode()).getElementsByTagName("Verteilung");
        for (int i = 0; i < verteilung.getLength(); i++) {
            final NodeList boats = ((Element) verteilung.item(i)).getElementsByTagName("Boat");
            for (int j = 0; j < boats.getLength(); j++) {
                result.add(new BoatImpl(boats.item(j)));
            }
        }
        return result;
    }

    @Override
    public Boat getBoatBySailID(String sailID) {
        for (Boat boat : getBoats()) {
            if (sailID.equals(boat.getSailingNumber())) {
                return boat;
            }
        }
        return null;
    }

    @Override
    public Iterable<Integer> getRaceNumbers() {
        LinkedHashSet<Integer> result = new LinkedHashSet<Integer>();
        for (Boat boat : getBoats()) {
            for (BoatResultInRace results : boat.getResultsInRaces()) {
                result.add(results.getRaceNumber());
            }
        }
        return result;
    }
}
