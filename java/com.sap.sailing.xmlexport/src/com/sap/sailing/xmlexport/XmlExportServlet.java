package com.sap.sailing.xmlexport;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.server.gateway.SailingServerHttpServlet;

public class XmlExportServlet extends SailingServerHttpServlet {
    private static final long serialVersionUID = -6849138354942569249L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final String action = req.getParameter("domain");
        try {
            if ((action != null) && !"".equals(action)) {
                if ("leaderboard".equalsIgnoreCase(action)) {
                    final LeaderboardData leaderboardData = new LeaderboardData(req, res, getService(),
                            getSecurityService());
                    leaderboardData.perform();
                    return;
                } else if ("foiling".equalsIgnoreCase(action)) {
                    final FoilingData foilingData = new FoilingData(req, res, getService(), getSecurityService());
                	foilingData.perform();
                	return;
                }
                throw new ServletException("Unknown domain " + action);
            }
            throw new ServletException("Please use the domain= parameter to specify a domain that'll be exported.");
        } catch (Exception e) {
            throw (new ServletException(e));
        }
    }
}
