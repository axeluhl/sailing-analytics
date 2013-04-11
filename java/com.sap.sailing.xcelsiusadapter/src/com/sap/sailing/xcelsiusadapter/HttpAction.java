package com.sap.sailing.xcelsiusadapter;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

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

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.util.InvalidDateException;

public abstract class HttpAction {
    private HttpServletRequest req;
    private HttpServletResponse res;

    private RacingEventService service;

    private Document table;

    private Element data;
    private final int maxRows;
    private int rowCount;
    private Element currentRow;

    public HttpAction(HttpServletRequest req, HttpServletResponse res, RacingEventService service, int maxRows) {
        this.req = req;
        this.res = res;
        this.service = service;
        this.maxRows = maxRows;
    }

    public String getAttribute(String name) {
        return this.req.getParameter(name);
    }

    public RacingEventService getService() {
        return service;
    }

    public HashMap<String, Regatta> getRegattas() {
        final HashMap<String, Regatta> result = new HashMap<String, Regatta>();
        for (final Regatta regatta : this.service.getAllRegattas()) {
            result.put(regatta.getName(), regatta);
        }
        return result;
    }

    public Regatta getRegatta() throws IOException {
        /*
         * REGATTA
         */
        final String regattaName = getAttribute("regatta");
        if (regattaName == null) {
            say("Use the regatta= parameter to specify the regatta");
            return null;
        }
        final Regatta regatta = getEvent(regattaName);
        if (regatta == null) {
            say("Regatta " + regattaName + " not found.");
            return null;
        }
        return regatta;
    }
    
    public RegattaLeaderboard getRegattaLeaderboard() throws IOException {
        /*
         * Leaderboard name is always equal Regatta name
         */
        final String leaderboardName = getAttribute("regatta");
        if (leaderboardName == null) {
            say("Use the regatta= parameter to specify the regatta");
            return null;
        }
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName); 
        if (leaderboard == null || !(leaderboard instanceof RegattaLeaderboard)) {
            say("Regatta " + leaderboardName + " not found.");
            return null;
        }
        return (RegattaLeaderboard)leaderboard;
    }

    public Regatta getEvent(String name) {
        for (final Regatta regatta : this.service.getAllRegattas()) {
            if (name.equals(regatta.getName())) {
                return regatta;
            }
        }

        return null;
    }

    public RaceDefinition getRace(Regatta regatta) throws IOException {
        final String raceName = getAttribute("race");

        if (raceName == null) {
            say("Use the race= parameter to specify the race.");

            return null;
        }

        final RaceDefinition race = getRace(regatta, raceName);

        if (race == null) {
            say("Race " + raceName + " not found.");

            return null;
        }

        return race;
    }

    public HashMap<String, RaceDefinition> getRaces(Regatta regatta) {
        final HashMap<String, RaceDefinition> result = new HashMap<String, RaceDefinition>();

        for (final RaceDefinition race : regatta.getAllRaces()) {
            result.put(race.getName(), race);
        }

        return result;
    }

    public RaceDefinition getRace(Regatta regatta, String name) {
        if ((regatta != null) && (name != null)) {
            for (RaceDefinition race : regatta.getAllRaces()) {
                if (name.equals(race.getName())) {
                    return race;
                }
            }
        }

        return null;
    }

    public TrackedRace getTrackedRace(Regatta regatta, RaceDefinition race) throws IOException {
        DynamicTrackedRegatta trackedRegatta = getService().getOrCreateTrackedRegatta(regatta);
        TrackedRace trackedRace = trackedRegatta == null ? null : trackedRegatta.getExistingTrackedRace(race);
        return trackedRace;
    }

    public TimePoint getTimePoint(TrackedRace race) throws IOException, InvalidDateException {
        final TimePoint start = race.getStartOfRace();
        final String time = getAttribute("time");
        if (time == null) {
            return start;
        }
        if ((time != null) && (time.length() > 0)) {
            if (start == null) {
                return null;
            }
            final long mTime = start.asMillis() + (stringToInteger(time, 0) * 1000 * 60);
            final TimePoint bTimePoint = new MillisecondsTimePoint(mTime);
            return bTimePoint;
        }
        return null;
    }

    private int stringToInteger(String strString, int intDefault) {
        try {
            return Integer.parseInt(strString);
        } catch (NumberFormatException e) {
            return intDefault;
        }
    }

    public Document getTable(String variable) {
        this.table = new Document();
        final Element data = new Element("data");
        this.table.addContent(data);
        final Element var = new Element("variable");
        var.setAttribute("name", variable);
        data.addContent(var);
        this.data = var;
        return this.table;
    }

    public void addRow() {
        if (maxRows == -1 || rowCount++ < maxRows) {
            final Element row = new Element("row");
            this.currentRow = row;
            this.data.addContent(row);
        }
    }

    public void addColumn(String content) {
        if (maxRows == -1 || rowCount <= maxRows) {
            final Element col = new Element("column");
            col.setText(content);
            this.currentRow.addContent(col);
        }
    }

    public void addNamedColumn(String content, String columnName) {
        if (maxRows == -1 || rowCount <= maxRows) {
            final Element col = new Element(columnName);
            col.setText(content);
            this.currentRow.addContent(col);
        }
    }

    public static void say(String msg, HttpServletResponse res) throws IOException {
        final Document doc = new Document();

        final Element el = new Element("message");
        el.setText(msg);

        doc.addContent(el);
        res.setCharacterEncoding("UTF-8");
        res.getWriter().print(getXMLAsString(doc));
    }

    public void say(Document doc) throws IOException {
        this.res.setCharacterEncoding("UTF-8");
        this.res.getWriter().print(getXMLAsString(doc));
    }

    public void say(String msg) throws IOException {
        say(msg, this.res);
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
}
