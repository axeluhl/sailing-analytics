package com.sap.sailing.kiworesultimport.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.kiworesultimport.Boat;
import com.sap.sailing.kiworesultimport.ResultList;

public class ResultListImpl extends AbstractNodeWrapper implements ResultList {
    private static final Logger logger = Logger.getLogger(ResultListImpl.class.getName());
    
    private static final SimpleDateFormat df = new SimpleDateFormat("dd. MMM yyyy hh:mm", Locale.GERMAN);
    
    public ResultListImpl(Node node) {
        super(node);
    }

    @Override
    public String getLegende() {
        return getNode().getAttributes().getNamedItem("legende").getNodeValue();
    }

    @Override
    public String getImagePfad() {
        return getNode().getAttributes().getNamedItem("imagePfad").getNodeValue();
    }

    @Override
    public String getStatus() {
        return getNode().getAttributes().getNamedItem("status").getNodeValue();
    }

    @Override
    public String getBoatClass() {
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
    public TimePoint getTimePoint() {
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
        final NodeList boats = ((Element) ((Element) getNode()).getElementsByTagName("Verteilung").item(0)).getElementsByTagName("Boat");
        for (int i=0; i<boats.getLength(); i++) {
            result.add(new BoatImpl(boats.item(i)));
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
}
