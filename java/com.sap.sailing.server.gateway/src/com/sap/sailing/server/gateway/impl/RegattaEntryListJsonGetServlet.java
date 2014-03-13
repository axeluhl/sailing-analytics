package com.sap.sailing.server.gateway.impl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.NationalityJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.PersonJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RegattaJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.TeamJsonSerializer;

public class RegattaEntryListJsonGetServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = -5280787192024697254L;

    private static final String PARAM_NAME_REGATTANAME = "name";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String regattaName = request.getParameter(PARAM_NAME_REGATTANAME);
        if (regattaName == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Need to specify a regatta name using the " + PARAM_NAME_REGATTANAME + " parameter");
        } else {
            Regatta regatta = getService().getRegatta(new RegattaName(regattaName));
            if (regatta == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Regatta " + regattaName
                        + " not found");
            } else {
            	NationalityJsonSerializer nationalityJsonSerializer = new NationalityJsonSerializer();
                CompetitorJsonSerializer competitorJsonSerializer = new CompetitorJsonSerializer(
                        new TeamJsonSerializer(new PersonJsonSerializer(nationalityJsonSerializer)), null);
                JsonSerializer<Regatta> regattaSerializer = new RegattaJsonSerializer(null, competitorJsonSerializer);
                JSONObject serializedRegatta = regattaSerializer.serialize(regatta);

                setJsonResponseHeader(response);
                serializedRegatta.writeJSONString(response.getWriter());
            }
        }
    }
}
