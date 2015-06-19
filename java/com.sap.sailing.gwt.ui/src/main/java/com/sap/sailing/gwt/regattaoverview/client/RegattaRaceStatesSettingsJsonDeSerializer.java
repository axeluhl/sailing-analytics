package com.sap.sailing.gwt.regattaoverview.client;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.sap.sailing.gwt.ui.client.GwtJsonDeSerializer;

public class RegattaRaceStatesSettingsJsonDeSerializer implements GwtJsonDeSerializer<RegattaRaceStatesSettings> {
    
    private static final String FIELD_VISIBLE_COURSE_AREAS = "visibleCourseAreas";
    private static final String FIELD_VISIBLE_REGATTAS = "visibleRegattas";
    private static final String FIELD_SHOW_ONLY_CURRENTLY_RUNNING_RACES = "showOnlyCurrentlyRunningRaces";
    private static final String FIELD_SHOW_ONLY_RACES_OF_SAME_DAY = "showOnlyRacesOfSameDay";

    @Override
    public JSONObject serialize(RegattaRaceStatesSettings settings) {
        JSONObject result = new JSONObject();
        result.put(FIELD_SHOW_ONLY_CURRENTLY_RUNNING_RACES, JSONBoolean.getInstance(settings.isShowOnlyCurrentlyRunningRaces()));
        result.put(FIELD_SHOW_ONLY_RACES_OF_SAME_DAY, JSONBoolean.getInstance(settings.isShowOnlyRacesOfSameDay()));
        
        JSONArray visibleCourseAreas = new JSONArray();
        for (int i = 0 ; i < settings.getVisibleCourseAreas().size() ; i++) {
            visibleCourseAreas.set(i, new JSONString(settings.getVisibleCourseAreas().get(i).toString()));
        }
        result.put(FIELD_VISIBLE_COURSE_AREAS, visibleCourseAreas);
        
        JSONArray visibleRegattas = new JSONArray();
        for (int i = 0 ; i < settings.getVisibleRegattas().size() ; i++) {
            visibleRegattas.set(i, new JSONString(settings.getVisibleRegattas().get(i)));
        }
        result.put(FIELD_VISIBLE_REGATTAS, visibleRegattas);
        
        return result;
    }

    @Override
    public RegattaRaceStatesSettings deserialize(JSONObject object) {
        if (object == null) {
            return null;
        }
        
        JSONBoolean showOnlyCurrentlyRunningRaces = (JSONBoolean) object.get(FIELD_SHOW_ONLY_CURRENTLY_RUNNING_RACES);
        JSONBoolean showOnlyRacesOfSameDay = (JSONBoolean) object.get(FIELD_SHOW_ONLY_RACES_OF_SAME_DAY);
        
        JSONArray jsonVisibleCourseAreas = (JSONArray) object.get(FIELD_VISIBLE_COURSE_AREAS);
        List<UUID> visibleCourseAreas = new ArrayList<UUID>();
        for (int i = 0 ; i < jsonVisibleCourseAreas.size() ; i++) {
            JSONString jsonCourseArea = (JSONString) jsonVisibleCourseAreas.get(i);
            visibleCourseAreas.add(UUID.fromString(jsonCourseArea.stringValue()));
        }
        
        JSONArray jsonVisibleRegattas = (JSONArray) object.get(FIELD_VISIBLE_REGATTAS);
        List<String> visibleRegattas = new ArrayList<String>();
        for (int i = 0 ; i < jsonVisibleRegattas.size() ; i++) {
            JSONString jsonRegatta = (JSONString) jsonVisibleRegattas.get(i);
            visibleRegattas.add(jsonRegatta.stringValue());
        }
        
        return new RegattaRaceStatesSettings(visibleCourseAreas, visibleRegattas, 
                showOnlyRacesOfSameDay.booleanValue(), showOnlyCurrentlyRunningRaces.booleanValue());
    }

}
