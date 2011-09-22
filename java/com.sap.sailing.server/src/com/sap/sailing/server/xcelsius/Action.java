package com.sap.sailing.server.xcelsius;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.util.InvalidDateException;

import org.jdom.Document;
import org.jdom.Element;

import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.IOException;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class Action {
  private HttpServletRequest  req;
  private HttpServletResponse res;

  private RacingEventService  service;

  private Document            table;

  private Element             data;
  private final int maxRows;
  private int rowCount;
  private Element             currentRow;

  public Action(HttpServletRequest req, HttpServletResponse res, RacingEventService service, int maxRows) {
    this.req     = req;
    this.res     = res;
    this.service = service;
    this.maxRows = maxRows;
  }

  public String getAttribute(String name) {
    return this.req.getParameter(name);
  }


  public RacingEventService getService() {
    return service;
  }


  public HashMap<String, Event> getEvents() {
    final HashMap<String, Event> result = new HashMap<String, Event>();

    for (final Event event : this.service.getAllEvents()) {
      result.put(event.getName(), event);
    }

    return result;
  }


  public Event getEvent() throws IOException {
    /*
     * EVENT
     */
    final String eventName = getAttribute("event");

    if (eventName == null) {
      say("Use the event= parameter to specify the event");

      return null;
    }

    final Event event = getEvent(eventName);

    if (event == null) {
      say("Event " + eventName + " not found.");

      return null;
    }

    return event;
  }


  public Event getEvent(String name) {
    for (final Event event : this.service.getAllEvents()) {
      if (name.equals(event.getName())) {
        return event;
      }
    }

    return null;
  }


  public RaceDefinition getRace(Event event) throws IOException {
    /*
     * Get the race
     */
    final String raceName = getAttribute("race");

    if (raceName == null) {
      say("Use the race= parameter to specify the race.");

      return null;
    }

    /*
     * RACE
     */
    final RaceDefinition race = getRace(event, raceName);

    if (race == null) {
      say("Race " + raceName + " not found.");

      return null;
    }

    return race;
  }


  public HashMap<String, RaceDefinition> getRaces(Event event) {
    final HashMap<String, RaceDefinition> result = new HashMap<String, RaceDefinition>();

    for (final RaceDefinition race : event.getAllRaces()) {
      result.put(race.getName(), race);
    }

    return result;
  }


  public RaceDefinition getRace(Event event, String name) {
    if ((event != null) && (name != null)) {
      for (RaceDefinition race : event.getAllRaces()) {
        if (name.equals(race.getName())) {
          return race;
        }
      }
    }

    return null;
  }


  public TrackedRace getTrackedRace(Event event, RaceDefinition race) throws IOException {
    DynamicTrackedEvent trackedEvent = getService().getDomainFactory().getTrackedEvent(event);
    TrackedRace trackedRace = trackedEvent==null?null:trackedEvent.getTrackedRace(race);
    return trackedRace;
  }


  public TimePoint getTimePoint(TrackedRace race) throws IOException, InvalidDateException {
    final String time = getAttribute("time");

    if (time == null) {
      say("Use time= parameter");

      return null;
    }

    if ((time != null) && (time.length() > 0)) {
      final TimePoint aTimePoint = race.getStart();

      if (aTimePoint == null) {
        return null;
      }

      final long      mTime      = aTimePoint.asMillis() + (StringUtility.StringToInteger(time, 0) * 1000 * 60);

      final TimePoint bTimePoint = new MillisecondsTimePoint(mTime);

      return bTimePoint;
    }

    return null;
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


  public static void say(String msg, HttpServletResponse res) throws IOException {
    final Document doc = new Document();

    final Element  el  = new Element("message");
    el.setText(msg);

    doc.addContent(el);
    res.setCharacterEncoding("UTF-8");
    res.getWriter().print(getXMLAsString(doc));
  }


  public void say(Document doc) throws IOException {
    this.res.getWriter().print(getXMLAsString(doc));
    ;
  }


  public void say(String msg) throws IOException {
    say(msg, this.res);
  }


  public static String getXMLAsString(Object context) {
    return getXMLAsString(context, "UTF-8");
  }


  public static String getXMLAsString(Object context, String encoding) {
    //final XMLOutputter outputter = new XMLOutputter("  ", true, encoding);
    final Format format = Format.getPrettyFormat();
    format.setIndent("  ");
    format.setEncoding(encoding);

    final XMLOutputter outputter = new XMLOutputter(format);

    if (context instanceof Document) {
      return outputter.outputString((Document) context);
    }

    if (context instanceof Element) {
      return outputter.outputString((Element) context);
    }

    return "";
  }
}
