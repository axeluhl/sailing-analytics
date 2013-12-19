package com.sap.sailing.xcelsiusadapter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;

public abstract class ExportAction {
    private HttpServletRequest req;
    private HttpServletResponse res;

    private RacingEventService service;

    public ExportAction(HttpServletRequest req, HttpServletResponse res, RacingEventService service) {
        this.req = req;
        this.res = res;
        this.service = service;
    }

    public String getAttribute(String name) {
        return this.req.getParameter(name);
    }

    public RacingEventService getService() {
        return service;
    }

    public Leaderboard getLeaderboard() throws IOException, ServletException {
        final String leaderboardName = getAttribute("name");
        if (leaderboardName == null) {
            throw new ServletException("Use the name= parameter to specify the leaderboard");
        }
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName); 
        if (leaderboard == null) {
            throw new ServletException("Leaderboard " + leaderboardName + " not found.");
        }
        return leaderboard;
    }
    
    protected String cleanRaceName(String raceName) {
        String newRaceName = raceName;
        if (raceName.matches("[A-Za-z ]*\\d")) {
            Pattern regex = Pattern.compile("([A-Za-z ]*)(\\d)");
            Matcher matcher = regex.matcher(raceName);
            String raceNameFirstPart = matcher.replaceAll("$1");
            String raceNumber = matcher.replaceAll("$2");
            try {
                if (Integer.parseInt(raceNumber) < 10) {
                    raceNumber = "0" + raceNumber;
                    newRaceName = raceNameFirstPart + raceNumber;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return newRaceName;
    }
    
    protected String cleanSailId(String sailId, Competitor competitor) {
        if (sailId.matches("^[A-Z]{3}\\s[0-9]*")) {                                        
            Pattern regex = Pattern.compile("(^[A-Z]{3})\\s([0-9]*)");
            Matcher regexMatcher = regex.matcher(sailId);
            try {
                return regexMatcher.replaceAll("$1$2");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (sailId.matches("^[A-Z]{3}\\S[0-9]*")) {
            return sailId;
        } else if (sailId.matches("[0-9]*")){
            Nationality nationality = competitor.getTeam().getNationality();
            return (nationality==null ? "" : nationality.getThreeLetterIOCAcronym() + sailId);
        } 
        return sailId;
    }

    public void sendDocument(Element element, String fileName) {
        final Format format = Format.getPrettyFormat();
        format.setIndent("  ");
        format.setExpandEmptyElements(true);
        format.setEncoding("UTF-8");
        XMLOutputter xmlOutputter = new XMLOutputter(format);
        
        res.setContentType("text/xml");
        res.addHeader("Content-Disposition", "attachment; filename=" + fileName);
        
        try {
            xmlOutputter.output(element, res.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected long handleValue(TimePoint timepoint) {
        if (timepoint != null) {
            return timepoint.asMillis();
        }
        return 0;
    }
    
    protected String getBoatClassName(final Leaderboard leaderboard) {
        String result = null;
        if (leaderboard instanceof RegattaLeaderboard) { 
            result = ((RegattaLeaderboard) leaderboard).getRegatta().getBoatClass().getName();
        } else {
            for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                for (Fleet fleet : raceColumn.getFleets()) {
                    TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                    if (trackedRace != null) {
                        result = trackedRace.getRace().getBoatClass().getName();
                    }
                }
            }
        }
        return result;
    }

    protected void addNamedElementWithValue(Element parent, String newChildName, Integer i) {
        if (i == null) {
            addNamedElementWithValue(parent, newChildName, "0");
        } else {
            addNamedElementWithValue(parent, newChildName, i.toString());
        }

    }

    protected void addNamedElementWithValue(Element parent, String newChildName, Double dbl) {
        if (dbl == null) {
            addNamedElementWithValue(parent, newChildName, "0");
        } else {
            addNamedElementWithValue(parent, newChildName, dbl.toString());
        }

    }

    protected void addNamedElementWithValue(Element parent, String newChildName, Long l) {
        if (l == null) {
            addNamedElementWithValue(parent, newChildName, "0");
        } else {
            addNamedElementWithValue(parent, newChildName, l.toString());
        }
    }

    protected Element addNamedElementWithValue(Element parent, String newChildName, String value) {
        final Element newChild = new Element(newChildName);
        newChild.addContent(value);
        parent.addContent(newChild);
        return newChild;
    }

    protected Element createNamedElementWithValue(String elementName, String value) {
        final Element element = new Element(elementName);
        element.addContent(value);
        return element;
    }

    protected Element createNamedElementWithValue(String elementName, int value) {
        final Element element = new Element(elementName);
        element.addContent(String.valueOf(value));
        return element;
    }

    protected Element createNamedElementWithValue(String elementName, double value) {
        final Element element = new Element(elementName);
        element.addContent(String.valueOf(value));
        return element;
    }
    
}
