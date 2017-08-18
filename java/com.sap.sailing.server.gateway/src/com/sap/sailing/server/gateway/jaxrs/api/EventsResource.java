package com.sap.sailing.server.gateway.jaxrs.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.dto.SeriesCreationParametersDTO;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseAreaJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.EventBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.EventRaceStatesSerializer;
import com.sap.sailing.server.gateway.serialization.impl.LeaderboardGroupBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.VenueJsonSerializer;
import com.sap.sailing.server.operationaltransformation.AddCourseAreas;
import com.sap.sailing.server.operationaltransformation.AddSpecificRegatta;
import com.sap.sailing.server.operationaltransformation.CreateEvent;
import com.sap.sailing.server.operationaltransformation.CreateLeaderboardGroup;
import com.sap.sailing.server.operationaltransformation.CreateRegattaLeaderboard;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.shared.media.ImageDescriptor;
import com.sap.sse.shared.media.VideoDescriptor;


@Path("/v1/events")
public class EventsResource extends AbstractSailingServerResource {

    private static final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    
    // SecurityUtils.getSubject().checkPermission(Permission.LEADERBOARD.getStringPermissionForObjects(Mode.UPDATE, leaderboardName));
    
    @POST
    @Path("/createEvent")
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response createEvent(
            @FormParam("eventName") @NotNull String eventName,
            @FormParam("eventDescription") String eventDescription,
            @FormParam("startDate") String startDateString,
            @FormParam("endDate") String endDateString,
            @FormParam("venueName") @NotNull String venueName,
            @FormParam("isPublic") @DefaultValue("false") boolean isPublic,
            @FormParam("officialWebsiteURL") String officialWebsiteURLString,
            @FormParam("baseURL") String baseURLString,
            @FormParam("leaderboardGroupIds") List<String> leaderboardGroupIdsList)
            throws ParseException, MalformedURLException {
        
        if(eventName == null){
            return isRequired("eventName");
        }
        
        if(venueName == null){
            return isRequired("venueName");
        }
        
        MillisecondsTimePoint startDate = null;
        try{
            startDate = startDateString != null ? new MillisecondsTimePoint(df.parse(startDateString)) : new MillisecondsTimePoint(new Date());
        }
        catch (ParseException e){
            return invalidDateFormat(startDateString);
        }
        
        MillisecondsTimePoint endDate = null;
        try{
            endDate = endDateString != null ? new MillisecondsTimePoint(df.parse(endDateString)) : new MillisecondsTimePoint(addOneWeek(startDate.asDate()));
        }
        catch (ParseException e){
            return invalidDateFormat(endDateString);
        }
        
        URL officialWebsiteURL  = null;
        try{
            officialWebsiteURL = officialWebsiteURLString != null ? new URL(officialWebsiteURLString) : null;
        }
        catch (MalformedURLException e){
            return invalidURLFormat(officialWebsiteURLString);
        }
        
        URL baseURL  = null;
        try{
            baseURL = baseURLString != null ? new URL(baseURLString) : null;
        }
        catch (MalformedURLException e){
            return invalidURLFormat(baseURLString);
        }
        
        List<UUID> leaderboardGroupIds = leaderboardGroupIdsList.stream().map(id -> UUID.fromString(id))
                .collect(Collectors.toList());
        
        UUID eventId = UUID.randomUUID();

        // ignoring sailorsInfoWebsiteURLs, images, videos
        Map<Locale, URL> sailorsInfoWebsiteURLs = null;
        Iterable<ImageDescriptor> images = Collections.<ImageDescriptor> emptyList();
        Iterable<VideoDescriptor> videos = Collections.<VideoDescriptor> emptyList();

        Event event = getService().apply(
                new CreateEvent(eventName, eventDescription, startDate, endDate, venueName, isPublic, eventId,
                        officialWebsiteURL, baseURL, sailorsInfoWebsiteURLs, images, videos, leaderboardGroupIds));

        return ok(event.getId().toString(), MediaType.TEXT_PLAIN);
    }



    @POST
    @Path("/addRegatta")
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response addRegatta(
            @FormParam("regattaName") @NotNull String regattaName,
            @FormParam("boatClassName") @NotNull String boatClassNameParam,
            @FormParam("startDate") String startDateString,
            @FormParam("eventId") String eventIdParam,
            @FormParam("endDate") String endDateString,
            @FormParam("persistent") @DefaultValue("true") boolean persistent,
            @FormParam("scoringScheme") @DefaultValue("LOW_POINT") String scoringSchemeParam,
            @FormParam("courseAreaId") String courseAreaIdParam,
            @FormParam("buoyZoneRadiusInHullLengths") @DefaultValue("3.0") double buoyZoneRadiusInHullLengths,
            @FormParam("useStartTimeInterference") @DefaultValue("true") boolean useStartTimeInterference,
            @FormParam("controlTrackingFromStartAndFinishTimes") @DefaultValue("false") boolean controlTrackingFromStartAndFinishTimes,
            @FormParam("rankingMetric") @DefaultValue("ONE_DESIGN") String rankingMetricParam)
            throws ParseException {
        
        if(regattaName == null){
            return isRequired("regattaName");
        }
        
        if(boatClassNameParam == null){
            return isRequired("boatClassName");
        }
        
        if(eventIdParam == null && courseAreaIdParam == null){
            return atLeastOneParameterRequired(Arrays.asList("eventId", "courseAreaId"));
        }
        
        UUID courseAreaId = null;
        
        if (eventIdParam != null) {
            UUID eventId = null;
            Event event = null;
            try {
                eventId = UUID.fromString(eventIdParam);
            } catch (IllegalArgumentException e) {
                return invalidIdFormat(eventIdParam);
            }
            try {
                event = getService().getEvent(eventId);
            } catch (IllegalArgumentException e) {
                return getBadEventErrorResponse(eventIdParam);
            }

            CourseArea courseArea = StreamSupport.stream(event.getVenue().getCourseAreas().spliterator(), false)
                    .filter(c -> c.getName().equals("Default")).findFirst().orElse(null);
            courseAreaId = courseArea != null ? courseArea.getId() : null;
        }
        
        if (courseAreaIdParam != null) {
            try {
                courseAreaId = UUID.fromString(courseAreaIdParam);
            } catch (IllegalArgumentException e) {
                return invalidIdFormat(courseAreaIdParam);
            }
        }

        String boatClassName = null;
        try{
            boatClassName = BoatClassMasterdata.valueOf(boatClassNameParam).name();
        }
        catch(IllegalArgumentException e){
            String correctValues = getEnumValuesAsString(BoatClassMasterdata.class);
            return valueNotKnown(boatClassNameParam, correctValues);
        }
        
        MillisecondsTimePoint startDate = null;
        try{
            startDate = startDateString != null ? new MillisecondsTimePoint(df.parse(startDateString)) : null;
        }
        catch (ParseException e){
            return invalidDateFormat(startDateString);
        }
        
        MillisecondsTimePoint endDate = null;
        try{
            endDate = endDateString != null ? new MillisecondsTimePoint(df.parse(endDateString)) : null;
        }
        catch (ParseException e){
            return invalidDateFormat(endDateString);
        }
        
        ScoringScheme scoringScheme = null;
        ScoringSchemeType type = null;
        if (scoringSchemeParam != null) {
            try {
                type = ScoringSchemeType.valueOf(scoringSchemeParam);
                scoringScheme = getService().getBaseDomainFactory().createScoringScheme(type);
            } catch (IllegalArgumentException e) {
                String correctValues = getEnumValuesAsString(ScoringSchemeType.class);
                return valueNotKnown(scoringSchemeParam, correctValues);
            }
        }
        
        RankingMetrics rankingMetric = null;
        if (rankingMetricParam != null) {
            try {
                rankingMetric = RankingMetrics.valueOf(rankingMetricParam);
            } catch (IllegalArgumentException e) {
                String correctValues = getEnumValuesAsString(RankingMetrics.class);
                return valueNotKnown(rankingMetricParam, correctValues);
            }
        }
        
        
        UUID regattaId = UUID.randomUUID();
        
        // using default seriesCreationParameters
        RegattaCreationParametersDTO regattaCreationParametersDTO = new RegattaCreationParametersDTO(
                createDefaultSeriesCreationParameters());
        
        AddSpecificRegatta operation = new AddSpecificRegatta(regattaName, boatClassName, startDate, endDate,
                regattaId, regattaCreationParametersDTO, persistent, scoringScheme, courseAreaId,
                buoyZoneRadiusInHullLengths, useStartTimeInterference, controlTrackingFromStartAndFinishTimes,
                rankingMetric);
        
        Regatta regatta = getService().apply(operation);
        
        return ok(regatta.getId().toString(), MediaType.TEXT_PLAIN);
    }
    
    @POST
    @Path("/addCourseArea")
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response addCourseArea(
            @FormParam("eventId") @NotNull String eventIdParam,
            @FormParam("courseAreaName") @DefaultValue("Default") @NotNull String courseAreaName) {
        
        if(eventIdParam == null){
            return isRequired("eventId");
        }
        
        if(courseAreaName == null){
            return isRequired("courseAreaName");
        }
        
        UUID eventId = null;
        try {
            eventId = UUID.fromString(eventIdParam);
        } catch (IllegalArgumentException e) {
            return invalidIdFormat(eventIdParam);
        }
        
        String[] courseAreaNames = new String[]{ courseAreaName };
        UUID[] courseAreaIds = new UUID[] {UUID.randomUUID()};
        
        CourseArea[] courseAreas = null;
        CourseArea result = null;
        try{
            courseAreas = getService().apply(new AddCourseAreas(eventId, courseAreaNames, courseAreaIds));
            result = courseAreas[0];
        }
        catch(IllegalArgumentException e){
            return getBadEventErrorResponse(eventId.toString());
        }
         
        return  result != null ? ok(result.getId().toString(), MediaType.TEXT_PLAIN) : error("",Status.INTERNAL_SERVER_ERROR, MediaType.TEXT_PLAIN);
    }
    
    @POST
    @Path("/addLeaderboard")
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response addLeaderboard( 
            @FormParam("regattaName") @NotNull String regattaName,
            @FormParam("discardThreshold") List<Integer> discardThresholdsParam, 
            @FormParam("leaderboardGroupId") String leaderboardGroupId) {
        
        if(regattaName == null){
            return isRequired("regattaName");
        }
        
        int[] discardThresholds = discardThresholdsParam != null ? discardThresholdsParam.stream().mapToInt(i->i).toArray() : null;
        
        final RegattaLeaderboard leaderboard = getService()
                .apply(new CreateRegattaLeaderboard(new RegattaName(regattaName), regattaName, discardThresholds));
        
        

        return ok(leaderboard.getName(), MediaType.TEXT_PLAIN);
    }

    @POST
    @Path("/addLeaderboardGroup")
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response addLeaderboardGroup(@FormParam("eventId") @NotNull String eventIdParam,
            @FormParam("leaderboardGroupName") @NotNull String leaderboardGroupName,
            @FormParam("leaderboardGroupDescription") String leaderboardGroupDescription,
            @FormParam("leaderboardGroupDisplayName") String leaderboardGroupDisplayName,
            @FormParam("displayGroupsInReverseOrder") boolean displayGroupsInReverseOrder,
            @FormParam("leaderboardNames") List<String> leaderboardNames,
            @FormParam("overallLeaderboardDiscardThresholds") List<Integer> overallLeaderboardDiscardThresholdsParam, 
            @QueryParam("overallLeaderboardScoringSchemeType") String overallLeaderboardScoringSchemeTypeParam) {
        
        if(eventIdParam == null){
            return isRequired("eventId");
        }
        
        if(leaderboardGroupName == null){
            return isRequired("leaderboardGroupName");
        }
        
        UUID eventId = null;
        try {
            eventId = UUID.fromString(eventIdParam);
        } catch (IllegalArgumentException e) {
            return invalidIdFormat(eventIdParam);
        }
        
        ScoringSchemeType overallLeaderboardScoringSchemeType = null;    
        if (overallLeaderboardScoringSchemeTypeParam != null) {
            try {
                overallLeaderboardScoringSchemeType = ScoringSchemeType.valueOf(overallLeaderboardScoringSchemeTypeParam);
            } catch (IllegalArgumentException e) {
                String correctValues = getEnumValuesAsString(ScoringSchemeType.class);
                return valueNotKnown(overallLeaderboardScoringSchemeTypeParam, correctValues);
            }
        }
        
        Event event = getService().getEvent(eventId);
        if(event == null){
            return getBadEventErrorResponse(eventId.toString());
        }
        
        int[] overallLeaderboardDiscardThresholds = overallLeaderboardDiscardThresholdsParam != null ? overallLeaderboardDiscardThresholdsParam.stream().mapToInt(i->i).toArray() : null;
        
        LeaderboardGroup leaderboardGroup = null;
        try{
            leaderboardGroup = getService()
                    .apply(new CreateLeaderboardGroup(leaderboardGroupName, leaderboardGroupDescription,
                            leaderboardGroupDisplayName, displayGroupsInReverseOrder, leaderboardNames, overallLeaderboardDiscardThresholds, overallLeaderboardScoringSchemeType));
        }
        catch(IllegalArgumentException e){
            return leadeboardNotFound(leaderboardNames);
        }
       
        List<UUID> newLeaderboardGroupIds = new ArrayList<>();
        StreamSupport.stream(event.getLeaderboardGroups().spliterator(), false).forEach(lg -> newLeaderboardGroupIds.add(lg.getId()));
        newLeaderboardGroupIds.add(leaderboardGroup.getId());
        
        getService().updateEvent(event.getId(), event.getName(), event.getDescription(), event.getStartDate(), event.getEndDate(),
                event.getVenue().getName(), event.isPublic(), newLeaderboardGroupIds, event.getOfficialWebsiteURL(), event.getBaseURL(), 
                 event.getSailorsInfoWebsiteURLs(), event.getImages(), event.getVideos());

        return ok(leaderboardGroup.getId().toString(), MediaType.TEXT_PLAIN);
    }
    
    private <E extends Enum<E>> String getEnumValuesAsString(Class<E> e) {
        return EnumSet.allOf(e).stream().map(en -> en.name()).collect(Collectors.joining(", "));
    }

    private LinkedHashMap<String, SeriesCreationParametersDTO> createDefaultSeriesCreationParameters() {
        final LinkedHashMap<String, SeriesCreationParametersDTO> seriesCreationParameters = new LinkedHashMap<>();
        seriesCreationParameters.put("Default", new SeriesCreationParametersDTO(
                Arrays.asList(new FleetDTO("Default", 0, null)), false, false, false, false, null, false, null));
        return seriesCreationParameters;
    }

    private Response ok(String message, String mediaType) {
        return Response.ok(message).header("Content-Type", mediaType + ";charset=UTF-8").build();
    }
    
    private Response error(String message, Status status, String mediaType) {
        return Response.status(status).header("Content-Type", mediaType + ";charset=UTF-8").entity(message).build(); 
    }
    
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getEvents(@QueryParam("showNonPublic") String showNonPublic) {
        // TODO bug2589, bug3504: the following will require EVENT:READ permission; it requires cross-server links to be authentication aware...
        // SecurityUtils.getSubject().checkPermission(Permission.EVENT.getStringPermission(Permission.Mode.READ));
        JsonSerializer<EventBase> eventSerializer = new EventBaseJsonSerializer(new VenueJsonSerializer(new CourseAreaJsonSerializer()), new LeaderboardGroupBaseJsonSerializer());
        JSONArray result = new JSONArray();
        for (EventBase event : getService().getAllEvents()) {
            if ((showNonPublic != null && Boolean.valueOf(showNonPublic)) || event.isPublic()) {
                result.add(eventSerializer.serialize(event));
            }
        }
        String json = result.toJSONString();
        return Response.ok(json).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{eventId}")
    public Response getEvent(@PathParam("eventId") String eventId) {
        // TODO bug2589, bug3504: the following will require EVENT:READ permission; it requires cross-server links to be authentication aware...
        // SecurityUtils.getSubject().checkPermission(Permission.EVENT.getStringPermissionForObjects(Permission.Mode.READ, eventId));
        Response response;
        UUID eventUuid;
        try {
            eventUuid = UUID.fromString(eventId);
        } catch (IllegalArgumentException e) {
            return getBadEventErrorResponse(eventId);
        }
        Event event = getService().getEvent(eventUuid);
        if (event == null) {
            response = getBadEventErrorResponse(eventId);
        } else {
            JsonSerializer<EventBase> eventSerializer = new EventBaseJsonSerializer(new VenueJsonSerializer(
                    new CourseAreaJsonSerializer()), new LeaderboardGroupBaseJsonSerializer());
            JSONObject eventJson = eventSerializer.serialize(event);

            String json = eventJson.toJSONString();
            response = Response.ok(json).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
        }
        return response;
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{eventId}/racestates")
    public Response getRaceStates(@PathParam("eventId") String eventId, @QueryParam("filterByLeaderboard") String filterByLeaderboard,
            @QueryParam("filterByCourseArea") String filterByCourseArea, @QueryParam("filterByDayOffset") String filterByDayOffset,
            @QueryParam("clientTimeZoneOffsetInMinutes") Integer clientTimeZoneOffsetInMinutes) {
        // TODO bug2589, bug3504: the following will require EVENT:READ permission; it requires cross-server links to be authentication aware...
        // SecurityUtils.getSubject().checkPermission(Permission.EVENT.getStringPermissionForObjects(Permission.Mode.READ, eventId));
        Response response;
        UUID eventUuid;
        try {
            eventUuid = UUID.fromString(eventId);
        } catch (IllegalArgumentException e) {
            return getBadEventErrorResponse(eventId);
        }
        Event event = getService().getEvent(eventUuid);
        if (event == null) {
            response = getBadEventErrorResponse(eventId);
        } else {
            final Duration clientTimeZoneOffset;
            if (filterByDayOffset != null) {
                if (clientTimeZoneOffsetInMinutes != null) {
                    clientTimeZoneOffset = new MillisecondsDurationImpl(1000*60*clientTimeZoneOffsetInMinutes);
                } else {
                    clientTimeZoneOffset = Duration.NULL;
                }
            } else {
                clientTimeZoneOffset = null;
            }
            EventRaceStatesSerializer eventRaceStatesSerializer = new EventRaceStatesSerializer(filterByCourseArea,
                    filterByLeaderboard, filterByDayOffset, clientTimeZoneOffset, getService());
            JSONObject raceStatesJson = eventRaceStatesSerializer.serialize(new Pair<Event, Iterable<Leaderboard>>(event, getService().getLeaderboards().values()));
            String json = raceStatesJson.toJSONString();
            response = Response.ok(json).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
        }
        return response;
    }
    
    private Map<Locale, URL> convertToLocalesAndUrls(Map<String, String> sailorsInfoWebsiteURLsByLocaleName) {
        Map<Locale, URL> eventURLs = new HashMap<>();
        for (Map.Entry<String, String> entry : sailorsInfoWebsiteURLsByLocaleName.entrySet()) {
            if (entry.getValue() != null) {
                try {
                    eventURLs.put(toLocale(entry.getKey()), new URL(entry.getValue()));
                } catch(Exception e) {
                    // broken URLs or Locales are not being stored
                }
            }
        }
        return eventURLs;
    }
    
    private Locale toLocale(String localeName) {
        if(localeName == null || localeName.isEmpty()) {
            return null;
        }
        return Locale.forLanguageTag(localeName);
    }
    
    private Response getBadEventErrorResponse(String eventId) {
        return Response.status(Status.NOT_FOUND).entity("Could not find an event with id '" + StringEscapeUtils.escapeHtml(eventId) + "'.").type(MediaType.TEXT_PLAIN).build();
    }
    
    private Response couldNotFind(Object object, String id) {
        return Response.status(Status.NOT_FOUND).entity("Could not find object of type "+object.getClass().getSimpleName()+" with id '" + StringEscapeUtils.escapeHtml(id) + "'.").type(MediaType.TEXT_PLAIN).build();
    }
    
    private Response alreadyExists(Object object, String id) {
        return Response.status(Status.BAD_REQUEST).entity("The object of type "+object.getClass().getSimpleName()+" with id '" + StringEscapeUtils.escapeHtml(id) + "' already exists.").type(MediaType.TEXT_PLAIN).build();
    }
    
    private Response isRequired(String parameter) {
        return Response.status(Status.BAD_REQUEST).entity("The parameter "+StringEscapeUtils.escapeHtml(parameter)+" is required.").type(MediaType.TEXT_PLAIN).build();
    }
    
    private Response invalidDateFormat(String date){
        return Response.status(Status.BAD_REQUEST).entity("The date "+StringEscapeUtils.escapeHtml(date)+" does not follow the date pattern \"dd-MM-yyyy\".").type(MediaType.TEXT_PLAIN).build();
    }
    
    private Response invalidURLFormat(String url){
        return Response.status(Status.BAD_REQUEST).entity("The format of the url "+StringEscapeUtils.escapeHtml(url)+" is incorrect.").type(MediaType.TEXT_PLAIN).build();
    }
    
    private Response invalidIdFormat(String id){
        return Response.status(Status.BAD_REQUEST).entity("The format of the id "+StringEscapeUtils.escapeHtml(id)+" is incorrect.").type(MediaType.TEXT_PLAIN).build();
    }
    
    private Response valueNotKnown(String value, String correctValues){
        return Response.status(Status.BAD_REQUEST).entity("The value \""+StringEscapeUtils.escapeHtml(value)+"\" is not recognized. Correct values are: "+correctValues).type(MediaType.TEXT_PLAIN).build();
    }
    
    private Response leadeboardNotFound(List<String> leaderboardNames){
        return Response.status(Status.BAD_REQUEST).entity("One of the following leaderboards was not found: \""+StringEscapeUtils.escapeHtml(leaderboardNames.toString())+"\" was not found.").type(MediaType.TEXT_PLAIN).build();
    }
    
    private Response atLeastOneParameterRequired(List<String> parameters){
        return Response.status(Status.BAD_REQUEST).entity("At least one of the following parameters must be provided: "+parameters.stream().collect(Collectors.joining(", "))+"\" was not found.").type(MediaType.TEXT_PLAIN).build();
    }
    
    private Date addOneWeek(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.WEEK_OF_MONTH, 1);
        return c.getTime();
    }
}
 