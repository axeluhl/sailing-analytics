package com.sap.sailing.xcelsiusadapter;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.server.gateway.SailingServerHttpServlet;

public class XcelsiusServlet extends SailingServerHttpServlet {
    private static final long serialVersionUID = -6849138354941569249L;

    public XcelsiusServlet() {
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
                    final RankPerLegAction a = new RankPerLegAction(req, res, getService(), maxRows);
                    a.perform();
                    return;
                } else if ("getRankPerLeg2".equals(action)) {
                    new RankPerLeg2Action(req, res, getService(), maxRows).perform();
                    return;
                } else if ("gpsPerRace".equals(action)) {
                    final GPSPerRaceAction a = new GPSPerRaceAction(req, res, getService(), maxRows);
                    a.perform();
                    return;
                } else if ("listEvents".equals(action)) {
                    final ListEventsAction a = new ListEventsAction(req, res, getService(), maxRows);
                    a.perform();
                    return;
                } else if ("getRankPerRace".equals(action)) {
                    final RankPerRaceAction a = new RankPerRaceAction(req, res, getService(), maxRows);
                    a.perform();
                    return;                
                } else if ("getRegattaDataPerLeg".equals(action)) {
                    final RegattaDataPerLegAction a = new RegattaDataPerLegAction(req, res, getService(), maxRows);
                    a.perform();
                    return;
                }else if ("getRegattaList".equals(action)) {
                    final RegattaListAction a = new RegattaListAction(req, res, getService(), maxRows);
                    a.perform();
                    return;
                }else {
                }
                HttpAction.say("Unknown action", res);
                return;
            }
            HttpAction.say("Please use the action= parameter to specify an action.", res);
            return;
        } catch (Exception e) {
            throw (new ServletException(e));
        }
    }
}
