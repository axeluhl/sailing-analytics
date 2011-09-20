package com.sap.sailing.server.xcelsius;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.server.RacingEventService;

import org.jdom.Document;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ListEvents extends Action {
  public ListEvents(HttpServletRequest req, HttpServletResponse res, RacingEventService service, int maxRows) {
    super(req, res, service, maxRows);
  }

  public void perform() throws Exception {
    final Document               table  = getTable("data");

    final HashMap<String, Event> events = getEvents();

    for (final String eventName : events.keySet()) {
      final Event                           event = events.get(eventName);

      final HashMap<String, RaceDefinition> races = getRaces(event);

      for (final String raceName : races.keySet()) {
        addRow();
        addColumn(eventName);
        addColumn(raceName);
      }
    }

    say(table);
  }
}
