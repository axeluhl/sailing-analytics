package com.sap.sailing.selenium.api.coursetemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.JsonWrapper;
import com.sap.sse.common.Util.Pair;

public class CourseTemplate extends JsonWrapper {

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_TAGS = "tags";
    private static final String FIELD_OPTIONAL_IMAGE_URL = "optionalImageURL";
    private static final String FIELD_ALL_MARK_TEMPLATES = "allMarkTemplates";
    private static final String FIELD_WAYPOINTS = "waypoints";
    private static final String FIELD_ASSOCIATED_ROLE = "associatedRole";
    private static final String FIELD_OPTIONAL_REPEATABLE_PART = "optionalRepeatablePart";

    private static final String FIELD_REPEATABLE_PART_START = "zeroBasedIndexOfRepeatablePartStart";
    private static final String FIELD_REPEATABLE_PART_END = "zeroBasedIndexOfRepeatablePartEnd";

    private final UUID id;
    private final String name;
    private URL optionalImageURL;
    private final Pair<Integer, Integer> optionalRepeatablePart;
    private final Iterable<String> tags;
    private final Iterable<MarkTemplate> allMarkTemplates;
    private final Map<MarkTemplate, String> roleMapping;
    private final Iterable<WaypointTemplate> waypoints;

    public CourseTemplate(JSONObject json) {
        super(json);
        name = get(FIELD_NAME);
        id = UUID.fromString(get(FIELD_ID));
        final String imageUrlStringOrNull = get(FIELD_OPTIONAL_IMAGE_URL);
        try {
            this.optionalImageURL = imageUrlStringOrNull == null ? null : new URL(imageUrlStringOrNull);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        JSONArray tagsJSON = get(FIELD_TAGS);
        if (tagsJSON == null) {
            tags = Collections.emptySet();
        } else {
            tags = tagsJSON.stream().map(Object::toString).collect(Collectors.toSet());
        }
        JSONArray markTemplatesJSON = get(FIELD_ALL_MARK_TEMPLATES);
        HashMap<UUID, MarkTemplate> markTemplatesById = new HashMap<>();
        roleMapping = new HashMap<>();
        markTemplatesJSON.forEach(o -> {
            final JSONObject mtJSON = (JSONObject) o;
            final MarkTemplate markTemplate = new MarkTemplate(mtJSON);
            markTemplatesById.put(markTemplate.getId(), markTemplate);
            String roleOrNull = (String) mtJSON.get(FIELD_ASSOCIATED_ROLE);
            if (roleOrNull != null) {
                roleMapping.put(markTemplate, roleOrNull);
            }
        });
        allMarkTemplates = markTemplatesById.values();
        final List<WaypointTemplate> waypoints = new ArrayList<WaypointTemplate>();
        JSONArray waypointsJSON = get(FIELD_WAYPOINTS);
        waypointsJSON.forEach(wpObject -> waypoints.add(new WaypointTemplate((JSONObject)wpObject, markTemplatesById::get)));
        this.waypoints = waypoints;
        final JSONObject repeatablePartJSON = get(FIELD_OPTIONAL_REPEATABLE_PART);
        if (repeatablePartJSON != null) {
            this.optionalRepeatablePart = new Pair<>(
                    ((Number) repeatablePartJSON.get(FIELD_REPEATABLE_PART_START)).intValue(),
                    ((Number) repeatablePartJSON.get(FIELD_REPEATABLE_PART_END)).intValue());
        } else {
            this.optionalRepeatablePart = null;
        }
    }

    public CourseTemplate(String name, List<MarkTemplate> allMarkTemplates, Map<MarkTemplate, String> roleMapping,
            Iterable<WaypointTemplate> waypoints, Pair<Integer, Integer> optionalRepeatablePart, Iterable<String> tags,
            URL optionalImageURL) {
        super(new JSONObject());
        this.optionalRepeatablePart = optionalRepeatablePart;
        this.optionalImageURL = optionalImageURL;
        this.id = null;
        this.name = name;
        this.allMarkTemplates = allMarkTemplates;
        this.roleMapping = roleMapping;
        this.waypoints = waypoints;
        this.tags = tags;
        
        getJson().put(FIELD_NAME, name);

        final JSONArray markTemplatesJSON = new JSONArray();
        allMarkTemplates.forEach(mt -> {
            final JSONObject markTemplateEntry = mt.getJson();
            final String roleOrNull = roleMapping.get(mt);
            if (roleOrNull != null) {
                markTemplateEntry.put(FIELD_ASSOCIATED_ROLE, roleOrNull);
            }
            markTemplatesJSON.add(markTemplateEntry);
        });
        getJson().put(FIELD_ALL_MARK_TEMPLATES, markTemplatesJSON);
        final JSONArray waypointsJSON = new JSONArray();
        waypoints.forEach(wpJSON -> waypointsJSON.add(wpJSON.getJson()));
        getJson().put(FIELD_WAYPOINTS, waypointsJSON);
        final JSONArray tagsJSON = new JSONArray();
        tags.forEach(tagsJSON::add);
        getJson().put(FIELD_TAGS, tagsJSON);
        if (optionalRepeatablePart != null) {
            final JSONObject repeatablePartJSON = new JSONObject();
            repeatablePartJSON.put(FIELD_REPEATABLE_PART_START, optionalRepeatablePart.getA());
            repeatablePartJSON.put(FIELD_REPEATABLE_PART_END, optionalRepeatablePart.getB());
            getJson().put(FIELD_OPTIONAL_REPEATABLE_PART, repeatablePartJSON);
        }
        if (optionalImageURL != null) {
            getJson().put(FIELD_OPTIONAL_IMAGE_URL, optionalImageURL.toExternalForm());
        }
    }

    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }

    public Iterable<String> getTags() {
        return tags;
    }

    public Iterable<MarkTemplate> getAllMarkTemplates() {
        return allMarkTemplates;
    }

    public Map<MarkTemplate, String> getRoleMapping() {
        return roleMapping;
    }

    public Iterable<WaypointTemplate> getWaypoints() {
        return waypoints;
    }

    public URL getOptionalImageURL() {
        return optionalImageURL;
    }

    public Pair<Integer, Integer> getOptionalRepeatablePart() {
        return optionalRepeatablePart;
    }
}