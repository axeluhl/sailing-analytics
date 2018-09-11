package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogTagEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.TagFinder;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sse.common.TimePoint;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.UserStore;

@Path("/v1/tags")
public class TagsResource extends AbstractSailingServerResource {

    /**
     * Serializes and deserializes {@link TagDTO tags}. <b>Before changing this class, have a look at
     * {@link TagDTO.TagDeSerializer}.</b>
     * 
     * @see TagDTO.TagDeSerializer
     */
    private class TagDTODeSerializer extends TagDTO.TagDeSerializer {

        private final JSONParser parser = new JSONParser();

        /**
         * Serializes {@link TagDTO tag} as {@link JSONObject}.
         * 
         * @param tag
         *            {@link TagDTO tag} to be serialized
         * @return {@link JSONObject json object}
         */
        private JSONObject serialize(TagDTO tag) {
            JSONObject result = new JSONObject();
            result.put(FIELD_TAG, tag.getTag());
            result.put(FIELD_COMMENT, tag.getComment());
            result.put(FIELD_IMAGE_URL, tag.getImageURL());
            result.put(FIELD_USERNAME, tag.getUsername());
            result.put(FIELD_VISIBLE_FOR_PUBLIC, tag.isVisibleForPublic());
            result.put(FIELD_RACE_TIMEPOINT, serializeTimePoint(tag.getRaceTimepoint()));
            result.put(FIELD_CREATED_AT, serializeTimePoint(tag.getCreatedAt()));
            result.put(FIELD_REVOKED_AT, serializeTimePoint(tag.getRevokedAt()));
            return result;
        }

        /**
         * Serializes list of {@link TagDTO tags} as {@link JSONArray}.
         * 
         * @param tags
         *            list of {@link TagDTO tags} to be serialized
         * @return {@link JSONArray json array}
         */
        private JSONArray serialize(List<TagDTO> tags) {
            JSONArray result = new JSONArray();
            for (TagDTO tag : tags) {
                result.add(serialize(tag));
            }
            return result;
        }

        /**
         * Deserializes {@link JSONObject jsonObject} to {@link TagDTO}.
         * 
         * @param jsonObject
         *            {@link JSONObject jsonObject} to be deserialized
         * @return {@link TagDTO tag}
         */
        private TagDTO deserialize(JSONObject jsonObject) {
            String tag = (String) jsonObject.get(FIELD_TAG);
            String comment = (String) jsonObject.get(FIELD_COMMENT);
            String imageURL = (String) jsonObject.get(FIELD_IMAGE_URL);
            String username = (String) jsonObject.get(FIELD_USERNAME);
            boolean visibleForPublic = (Boolean) jsonObject.get(FIELD_VISIBLE_FOR_PUBLIC);
            TimePoint raceTimePoint = deserilizeTimePoint((String) (jsonObject.get(FIELD_RACE_TIMEPOINT)));
            TimePoint createdAt = deserilizeTimePoint((String) (jsonObject.get(FIELD_CREATED_AT)));
            TimePoint revokedAt = deserilizeTimePoint((String) (jsonObject.get(FIELD_REVOKED_AT)));
            return new TagDTO(tag, comment, imageURL, username, visibleForPublic, raceTimePoint, createdAt, revokedAt);
        }

        /**
         * Deserializes {@link JSONArray jsonArray} to list of {@link TagDTO tags}.
         * 
         * @param jsonArray
         *            {@link JSONArray jsonArray} to be deserialized
         * @return list of {@link TagDTO tags}
         */
        private List<TagDTO> deserialize(JSONArray jsonArray) {
            return deserializeTags(jsonArray.toJSONString());
        }

        @Override
        public String serializeTag(TagDTO tag) {
            return serialize(tag).toString();
        }

        @Override
        public String serializeTags(List<TagDTO> tags) {
            JSONArray jsonTags = new JSONArray();
            for (int i = 0; i < tags.size(); i++) {
                jsonTags.set(i, serialize(tags.get(i)));
            }
            return jsonTags.toString();
        }

        @Override
        public TagDTO deserializeTag(String jsonObject) {
            JSONObject jsonTag;
            try {
                jsonTag = (JSONObject) parser.parse(jsonObject);
                return deserialize(jsonTag);
            } catch (ParseException e) {
                return null;
            }

        }

        @Override
        public List<TagDTO> deserializeTags(String jsonArray) {
            JSONArray jsonTags;
            List<TagDTO> result = new ArrayList<TagDTO>();
            try {
                jsonTags = (JSONArray) parser.parse(jsonArray);
                for (int i = 0; i < jsonTags.size(); i++) {
                    result.add(deserialize((JSONObject) jsonTags.get(i)));
                }
            } catch (ParseException e) {

            }
            return result;
        }
    }

    private final TagDTODeSerializer serializer;
    // private final boolean enforceSecurityChecks;

    public TagsResource() {
        this(true);
    }

    public TagsResource(boolean enforceSecurityChecks) {
        // this.enforceSecurityChecks = enforceSecurityChecks;
        serializer = new TagDTODeSerializer();
    }

    /**
     * Loads public tags from {@link RaceLog} and private tags from {@link UserStore} in case current user is logged in.
     * Only loads private tags when parameters <code>leaderboardName</code>, <code>raceColumnName</code> and
     * <code>fleetName</code> can identify {@link RaceLog}.
     */
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("/{leaderboardName}/{raceColumnName}/{fleetName}")
    public Response getTags(@PathParam("leaderboardName") String leaderboardName,
            @PathParam("raceColumnName") String raceColumnName, @PathParam("fleetName") String fleetName) {
        // default response
        Response response = Response.noContent().build();
        List<TagDTO> tags = new ArrayList<TagDTO>();

        // check if RaceLog is available and all parameters are set
        RaceLog raceLog = getService().getRaceLog(leaderboardName, raceColumnName, fleetName);
        if (raceLog == null) {
            response = Response.status(Status.PRECONDITION_FAILED)
                    .entity("RaceLog not found! Check parameters leaderboardName, raceColumnName and fleetName")
                    .build();
        } else {
            // load public tags
            TagFinder finder = new TagFinder(raceLog, true);
            List<RaceLogTagEvent> publicTags = finder.analyze();
            for (RaceLogTagEvent tagEvent : publicTags) {
                tags.add(new TagDTO(tagEvent.getTag(), tagEvent.getComment(), tagEvent.getImageURL(),
                        tagEvent.getUsername(), true, tagEvent.getLogicalTimePoint(), tagEvent.getCreatedAt()));
            }

            // load private tags
            Subject subject = SecurityUtils.getSubject();
            if (subject.getPrincipal() != null) {
                String username = subject.getPrincipal().toString();
                SecurityService securityService = getService(SecurityService.class);

                // TODO: check preferences key in home and set private tag
                String preference = securityService.getPreference(username,
                        serializer.generateUniqueKey(leaderboardName, raceColumnName, fleetName));
                if (preference != null) {
                    try {
                        JSONParser jsonParser = new JSONParser();
                        JSONArray jsonArray = (JSONArray) jsonParser.parse(preference);
                        List<TagDTO> privateTags = serializer.deserialize(jsonArray);
                        tags.addAll(privateTags);
                    } catch (ParseException e) {
                        // TODO: add error handling, could not read private tags
                    }
                }
            }

            // convert tags to JSON
            JSONArray jsonTags = serializer.serialize(tags);
            response = Response.ok(jsonTags.toJSONString()).build();
        }

        return response;
    }
}
