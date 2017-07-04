package com.sap.sailing.server.gateway.serialization.racegroup.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.racegroup.RaceCell;
import com.sap.sailing.domain.base.racegroup.RaceRow;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceRowJsonSerializer implements JsonSerializer<RaceRow> {
    public static final String FIELD_FLEET = "fleet";
    
    /**
     * The race cells in the JSONArray keyed by this name are expected to be written
     * in the order in which their race columns appear in the series / leaderboard
     */
    public static final String FIELD_RACE_CELLS = "races";

    private JsonSerializer<Fleet> fleetSerializer;
    private JsonSerializer<RaceCell> cellSerializer;

    public RaceRowJsonSerializer(
            JsonSerializer<Fleet> fleetSerializer,
            JsonSerializer<RaceCell> cellSerializer) {
        this.fleetSerializer = fleetSerializer;
        this.cellSerializer = cellSerializer;
    }

    @Override
    public JSONObject serialize(RaceRow object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_FLEET, fleetSerializer.serialize(object.getFleet()));
        JSONArray cells = new JSONArray(); 
        for (RaceCell cell : object.getCells()) {
            cells.add(cellSerializer.serialize(cell));
        }
        result.put(FIELD_RACE_CELLS, cells);
        return result;
    }

}
