package com.sap.sailing.simulator.impl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.Servlet;

public class SimulatorServlet extends Servlet {
	private static final long serialVersionUID = -5901084095272813626L;

	public SimulatorServlet() {
		super();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		RacingEventService service = getService();
		Iterable<Event> events = service.getAllEvents();
		for (Event event : events) {
			resp.getWriter().println(event.getName());
		}
	}

}
