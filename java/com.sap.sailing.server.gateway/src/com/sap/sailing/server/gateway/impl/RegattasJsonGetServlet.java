package com.sap.sailing.server.gateway.impl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.serialization.impl.RegattaJsonSerializer;

public class RegattasJsonGetServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = 1333207389294903999L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RegattaJsonSerializer regattaJsonSerializer = new RegattaJsonSerializer(); 
        
        JSONArray regattasJson = new JSONArray();
        for (Regatta regatta : getService().getAllRegattas()) {
            regattasJson.add(regattaJsonSerializer.serialize(regatta));
        }
        setJsonResponseHeader(resp);
        regattasJson.writeJSONString(resp.getWriter());
    }
}
