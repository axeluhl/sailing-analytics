package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.TopLevelMasterDataSerializer;

public class MasterDataByLeaderboardGroupJsonGetServlet extends AbstractJsonHttpServlet {

    private static final long serialVersionUID = 998103495657252850L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, LeaderboardGroup> leaderboardGroups = getService().getLeaderboardGroups();
        Set<String> requestedLeaderboardGroupNames = new HashSet<String>();

        String query = req.getQueryString();
        if (query != null && query.length() > 0) {
            String[] names = parseQuery(query);
            for (int i = 0; i < names.length; i++) {
                requestedLeaderboardGroupNames.add(names[i]);
            }
        } else {
            // No range supplied. Export all for now
            requestedLeaderboardGroupNames.addAll(leaderboardGroups.keySet());
        }

        TopLevelMasterDataSerializer masterSerializer = new TopLevelMasterDataSerializer(leaderboardGroups,
                getService().getAllEvents(), getService().getPersistentRegattasForRaceIDs(), getService().getAllMediaTracks());

        JSONObject masterData = masterSerializer.serialize(requestedLeaderboardGroupNames);

        setJsonResponseHeader(resp);
        masterData.writeJSONString(resp.getWriter());
    }

    private String[] parseQuery(String query) throws UnsupportedEncodingException {
        List<String> names = new ArrayList<String>();
        for (String pair : query.split("&")) {
            int eq = pair.indexOf("=");
            if (eq >= 0) {
                // key=value
                String key = URLDecoder.decode(pair.substring(0, eq), "UTF-8");
                String value = URLDecoder.decode(pair.substring(eq + 1), "UTF-8");
                if (key.contains("names")) {
                    names.add(value);
                }
            }
        }

        return names.toArray(new String[names.size()]);
    }

}
