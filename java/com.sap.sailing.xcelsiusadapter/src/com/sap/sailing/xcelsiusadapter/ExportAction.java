package com.sap.sailing.xcelsiusadapter;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.DOMOutputter;
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

    public void sendDocument(Document doc, String fileName) {
        ServletOutputStream stream = null;
        BufferedInputStream buf = null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Result outputTarget = new StreamResult(outputStream);
            DOMOutputter outputter = new DOMOutputter();

            org.w3c.dom.Document w3cDoc = outputter.output(doc);
            Source xmlSource = new DOMSource(w3cDoc);

            stream = res.getOutputStream();

            res.setContentType("text/xml");
            res.addHeader("Content-Disposition", "attachment; filename=" + fileName);
            TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
            InputStream is = new ByteArrayInputStream(outputStream.toByteArray());

            buf = new BufferedInputStream(is);
            int readBytes = 0;
            while ((readBytes = buf.read()) != -1)
                stream.write(readBytes);

        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JDOMException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            if (buf != null)
                try {
                    buf.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
    }

    public static String getXMLAsString(Object context) {
        return getXMLAsString(context, "UTF-8");
    }

    public static String getXMLAsString(Object context, String encoding) {
        // final XMLOutputter outputter = new XMLOutputter("  ", true, encoding);
        final Format format = Format.getPrettyFormat();
        format.setIndent("  ");
        // Despite of setting the expand true (result is <column></column> instead of <column/> browsers like firefox
        // dont display the columns correctly
        format.setExpandEmptyElements(true);
        format.setEncoding(encoding);

        final XMLOutputter outputter = new XMLOutputter(format);

        if (context instanceof Document) {
            String res = outputter.outputString((Document) context);
            return res;
        }

        if (context instanceof Element) {
            return outputter.outputString((Element) context);
        }

        return "";
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
