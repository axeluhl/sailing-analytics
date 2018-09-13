package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogTagEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.TagFinder;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.tagging.TagDTODeSerializer;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.UserStore;

@Path("/v1/tags")
public class TagsResource extends AbstractSailingServerResource {

    private static final Logger logger = Logger.getLogger(TagsResource.class.getName());

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
    public Response getTags(@PathParam("leaderboardName") String leaderboardNameParameter,
            @PathParam("raceColumnName") String raceColumnNameParameter,
            @PathParam("fleetName") String fleetNameParameter) {
        // restore escaped parameters
        String leaderboardName = restoreEscapedParameter(leaderboardNameParameter);
        String raceColumnName = restoreEscapedParameter(raceColumnNameParameter);
        String fleetName = restoreEscapedParameter(fleetNameParameter);

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
                        logger.warning("Could not parse private tags received from JSON in UserStore!");
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
