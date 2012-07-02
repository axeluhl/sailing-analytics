package com.sap.sailing.xcelsiusadapter;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.server.Servlet;

public class XcelsiusApp extends Servlet {
    private static final long serialVersionUID = -6849138354941569249L;

    public XcelsiusApp() {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final String action = req.getParameter("action");

        try {
            if ((action != null) && !"".equals(action)) {
                int maxRows = -1;
                try {
                    String maxRowsParam = req.getParameter("maxrows");
                    if (maxRowsParam != null) {
                        maxRows = Integer.valueOf(maxRowsParam);
                    }
                } catch (NumberFormatException nfe) {
                    // ignore and leave at -1
                }
                if ("getRankPerLeg".equals(action)) {
                    final RankPerLeg a = new RankPerLeg(req, res, getService(), maxRows);
                    a.perform();
                    return;
                } else if ("getRankPerLeg2".equals(action)) {
                    new RankPerLeg2(req, res, getService(), maxRows).perform();
                    return;
                } else if ("gpsPerRace".equals(action)) {
                    final GPSPerRace a = new GPSPerRace(req, res, getService(), maxRows);
                    a.perform();
                    return;
                } else if ("listEvents".equals(action)) {
                    final ListEvents a = new ListEvents(req, res, getService(), maxRows);
                    a.perform();
                    return;
                } else if ("getRankPerRace".equals(action)) {
                    final RankPerRace a = new RankPerRace(req, res, getService(), maxRows);
                    a.perform();
                    return;                
                } else if ("getRegattaDataPerLeg".equals(action)) {
                    final RegattaDataPerLeg a = new RegattaDataPerLeg(req, res, getService(), maxRows);
                    a.perform();
                    return;
                }else if ("getRegattaList".equals(action)) {
                    final RegattaList a = new RegattaList(req, res, getService(), maxRows);
                    a.perform();
                    return;
                }else {
                }
                Action.say("Unknown action", res);
                return;
            }
            Action.say("Please use the action= parameter to specify an action.", res);
            return;
        } catch (Exception e) {
            throw (new ServletException(e));
        }
    }
}
