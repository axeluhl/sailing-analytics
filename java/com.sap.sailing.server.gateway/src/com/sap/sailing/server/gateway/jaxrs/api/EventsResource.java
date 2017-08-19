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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Spliterator;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import org.apache.shiro.SecurityUtils;
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
import com.sap.sailing.domain.common.security.Permission;
import com.sap.sailing.domain.common.security.Permission.Mode;
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

    private static final Logger logger = Logger.getLogger(EventsResource.class.getName());
    
    private static final String DEFAULT_EVENT_IS_PUBLIC = "false";
    private static final String DEFAULT_EVENT_CREATE_LEADERBOARD_GROUP = "false";
    private static final String DEFAULT_EVENT_CREATE_REGATTA = "false";
    
    private static final String DEFAULT_LG_RANKING_METRIC = "ONE_DESIGN";
    private static final String DEFAULT_LG_OVERALL_LB_SCORING_SCHEME = "LOW_POINT";
    private static final String DEFAULT_LG_DISPLAY_NAME = null;
    private static final String DEFAULT_LG_DISPLAY_GROUPS_IN_REVERSE_ORDER = "false";
    private static final List<String> DEFAULT_LG_LEADERBOARD_NAMES = new ArrayList<String>();
    private static final List<Integer> DEFAULT_LG_OVERALL_LB_DISCARD_THRESHOLDS = new ArrayList<Integer>();
    
    private static final String DEFAULT_COURSE_AREA_NAME = "Default";
    
    private static final String DEFAULT_REGATTA_IS_PERSISTENT = "true";
    private static final String DEFAULT_REGATTA_BUOY_ZONE_RADIUS_IN_HULL_LENGTH = "3.0";
    private static final String DEFAULT_REGATTA_CONTROL_TRACKING_FROM_START_AND_FINISH_TIMES = "false";
    private static final String DEFAULT_REGATTA_USE_START_TIME_INTERFERENCE = "true";
    private static final String DEFAULT_REGATTA_START_DATE = null;
    private static final String DEFAULT_REGATTA_END_DATE = null;
    private static final String DEFAULT_REGATTA_COURSE_AREA_ID = null;
    
    private static final String DEFAULT_SERIES_NAME = "Default";
    
    private static final String DEFAULT_FLEET_NAME = "Default";
        
    private static final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");

    @POST
    @Path("/createEvent")
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response createEvent(@FormParam("eventName") String eventName,
            @FormParam("eventDescription") String eventDescription,
            @FormParam("startDate") String startDateString,
            @FormParam("endDate") String endDateString, 
            @FormParam("venueName") String venueName,
            @FormParam("isPublic") @DefaultValue(DEFAULT_EVENT_IS_PUBLIC) String isPublicParam,
            @FormParam("officialWebsiteURL") String officialWebsiteURLString,
            @FormParam("baseURL") String baseURLString,
            @FormParam("leaderboardGroupIds") List<String> leaderboardGroupIdsList,
            @FormParam("createLeaderboardGroup") @DefaultValue(DEFAULT_EVENT_CREATE_LEADERBOARD_GROUP) String createLeaderboardGroupParam,
            @FormParam("createRegatta") @DefaultValue(DEFAULT_EVENT_CREATE_REGATTA) String createRegattaParam,
            @FormParam("boatClassName") String boatClassName)
    {
        SecurityUtils.getSubject().checkPermission(Permission.EVENT.getStringPermission(Mode.CREATE));
        logger.fine(SecurityUtils.getSubject().getSession().toString());

        boolean createRegatta = Boolean.parseBoolean(createRegattaParam);
        boolean isPublic = Boolean.parseBoolean(isPublicParam);
        boolean createLeaderboardGroup = Boolean.parseBoolean(createLeaderboardGroupParam);
        
        if (eventName == null) {
            return getParameterMissingResponse("eventName");
        }

        if (venueName == null) {
            return getParameterMissingResponse("venueName");
        }
        
        if (createRegatta) {
            if (boatClassName == null) {
                return getParameterMissingResponse("boatClassName");
            }
        }

        if (eventDescription == null) {
            eventDescription = eventName;
        }

        MillisecondsTimePoint startDate = null;
        try {
            startDate = startDateString != null ? new MillisecondsTimePoint(df.parse(startDateString))
                    : new MillisecondsTimePoint(new Date());
        } catch (ParseException e) {
            return getInvalidDateFormatResponse(startDateString);
        }

        MillisecondsTimePoint endDate = null;
        try {
            endDate = endDateString != null ? new MillisecondsTimePoint(df.parse(endDateString))
                    : new MillisecondsTimePoint(addOneWeek(startDate.asDate()));
        } catch (ParseException e) {
            return getInvalidDateFormatResponse(endDateString);
        }

        URL officialWebsiteURL = null;
        try {
            officialWebsiteURL = officialWebsiteURLString != null ? new URL(officialWebsiteURLString) : null;
        } catch (MalformedURLException e) {
            return getInvalidURLFormatResponse(officialWebsiteURLString);
        }

        URL baseURL = null;
        try {
            baseURL = baseURLString != null ? new URL(baseURLString) : null;
        } catch (MalformedURLException e) {
            return getInvalidURLFormatResponse(baseURLString);
        }

        List<UUID> leaderboardGroupIds = leaderboardGroupIdsList.stream().map(id -> UUID.fromString(id))
                .collect(Collectors.toList());

        UUID eventId = UUID.randomUUID();

        // ignoring sailorsInfoWebsiteURLs, images, videos
        Map<Locale, URL> sailorsInfoWebsiteURLs = null;
        Iterable<ImageDescriptor> images = Collections.<ImageDescriptor> emptyList();
        Iterable<VideoDescriptor> videos = Collections.<VideoDescriptor> emptyList();

        Event event = getService()
                .apply(new CreateEvent(eventName, eventDescription, startDate, endDate, venueName, isPublic, eventId,
                        officialWebsiteURL, baseURL, sailorsInfoWebsiteURLs, images, videos, leaderboardGroupIds));

        addCourseArea(event.getId().toString(), DEFAULT_COURSE_AREA_NAME);

        if (createLeaderboardGroup) {
            addLeaderboardGroup(event.getId().toString(), event.getName(), event.getName(), DEFAULT_LG_DISPLAY_NAME, DEFAULT_LG_DISPLAY_GROUPS_IN_REVERSE_ORDER,
                    DEFAULT_LG_LEADERBOARD_NAMES, DEFAULT_LG_OVERALL_LB_DISCARD_THRESHOLDS, DEFAULT_LG_OVERALL_LB_SCORING_SCHEME);
        }

        if (createRegatta) {
            addRegatta(event.getName(), boatClassName, DEFAULT_REGATTA_START_DATE, event.getId().toString(), DEFAULT_REGATTA_END_DATE,
                    DEFAULT_REGATTA_IS_PERSISTENT, DEFAULT_LG_OVERALL_LB_SCORING_SCHEME, DEFAULT_REGATTA_COURSE_AREA_ID,
                    DEFAULT_REGATTA_BUOY_ZONE_RADIUS_IN_HULL_LENGTH,
                    DEFAULT_REGATTA_USE_START_TIME_INTERFERENCE,
                    DEFAULT_REGATTA_CONTROL_TRACKING_FROM_START_AND_FINISH_TIMES,
                    DEFAULT_LG_RANKING_METRIC);
        }

        return ok(event.getId().toString(), MediaType.TEXT_PLAIN);
    }

    @POST
    @Path("/addRegatta")
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response addRegatta(@FormParam("regattaName") String regattaName,
            @FormParam("boatClassName") String boatClassNameParam,
            @FormParam("startDate") String startDateString, 
            @FormParam("eventId") String eventIdParam,
            @FormParam("endDate") String endDateString,
            @FormParam("persistent") @DefaultValue(DEFAULT_REGATTA_IS_PERSISTENT) String isPersistentParam,
            @FormParam("scoringScheme") @DefaultValue(DEFAULT_LG_OVERALL_LB_SCORING_SCHEME) String scoringSchemeParam,
            @FormParam("courseAreaId") String courseAreaIdParam,
            @FormParam("buoyZoneRadiusInHullLengths") @DefaultValue(DEFAULT_REGATTA_BUOY_ZONE_RADIUS_IN_HULL_LENGTH) String buoyZoneRadiusInHullLengthsParam,
            @FormParam("useStartTimeInterference") @DefaultValue(DEFAULT_REGATTA_USE_START_TIME_INTERFERENCE) String useStartTimeInterferenceParam,
            @FormParam("controlTrackingFromStartAndFinishTimes") @DefaultValue(DEFAULT_REGATTA_CONTROL_TRACKING_FROM_START_AND_FINISH_TIMES) String controlTrackingFromStartAndFinishTimesParam,
            @FormParam("rankingMetric") @DefaultValue(DEFAULT_LG_RANKING_METRIC) String rankingMetricParam)
        {
        
        boolean isPersistent = Boolean.parseBoolean(isPersistentParam);
        boolean controlTrackingFromStartAndFinishTimes = Boolean.parseBoolean(controlTrackingFromStartAndFinishTimesParam);
        boolean useStartTimeInterference = Boolean.parseBoolean(useStartTimeInterferenceParam);
        double buoyZoneRadiusInHullLengths = Double.parseDouble(buoyZoneRadiusInHullLengthsParam);
        
        if (regattaName == null) {
            return getParameterMissingResponse("regattaName");
        }

        if (boatClassNameParam == null) {
            return getParameterMissingResponse("boatClassName");
        }

        if (eventIdParam == null && courseAreaIdParam == null) {
            return getMissingParametersResponse(Arrays.asList("eventId", "courseAreaId"));
        }

        UUID courseAreaId = null;

        if (eventIdParam != null) {
            UUID eventId = null;
            Event event = null;
            try {
                eventId = UUID.fromString(eventIdParam);
            } catch (IllegalArgumentException e) {
                return getInvalidIdFormatResponse(eventIdParam);
            }
            
            event = getService().getEvent(eventId);
            if (event == null){
                return getBadEventErrorResponse(eventIdParam);
            }   
            
            CourseArea courseArea = getDefaultCourseArea(event);
            courseAreaId = courseArea != null ? courseArea.getId() : null;
        }

        if (courseAreaIdParam != null) {
            try {
                courseAreaId = UUID.fromString(courseAreaIdParam);
            } catch (IllegalArgumentException e) {
                return getInvalidIdFormatResponse(courseAreaIdParam);
            }
        }

        String boatClassName = null;
        try {
            boatClassName = BoatClassMasterdata.valueOf(boatClassNameParam).name();
        } catch (IllegalArgumentException e) {
            String correctValues = getEnumValuesAsString(BoatClassMasterdata.class);
            return getValueUnknownResponse(boatClassNameParam, correctValues);
        }

        MillisecondsTimePoint startDate = null;
        try {            startDate = startDateString != null ? new MillisecondsTimePoint(df.parse(startDateString)) : new MillisecondsTimePoint(new Date());;
        } catch (ParseException e) {
            return getInvalidDateFormatResponse(startDateString);
        }

        MillisecondsTimePoint endDate = null;
        try {
            endDate = endDateString != null ? new MillisecondsTimePoint(df.parse(endDateString)) : new MillisecondsTimePoint(addOneWeek(startDate.asDate()));;
        } catch (ParseException e) {
            return getInvalidDateFormatResponse(endDateString);
        }

        ScoringScheme scoringScheme = null;
        ScoringSchemeType type = null;
        if (scoringSchemeParam != null) {
            try {
                type = ScoringSchemeType.valueOf(scoringSchemeParam);
                scoringScheme = getService().getBaseDomainFactory().createScoringScheme(type);
            } catch (IllegalArgumentException e) {
                String correctValues = getEnumValuesAsString(ScoringSchemeType.class);
                return getValueUnknownResponse(scoringSchemeParam, correctValues);
            }
        }

        RankingMetrics rankingMetric = null;
        if (rankingMetricParam != null) {
            try {
                rankingMetric = RankingMetrics.valueOf(rankingMetricParam);
            } catch (IllegalArgumentException e) {
                String correctValues = getEnumValuesAsString(RankingMetrics.class);
                return getValueUnknownResponse(rankingMetricParam, correctValues);
            }
        }

        UUID regattaId = UUID.randomUUID();

        // using default seriesCreationParameters
        RegattaCreationParametersDTO regattaCreationParametersDTO = new RegattaCreationParametersDTO(
                createDefaultSeriesCreationParameters());

        AddSpecificRegatta operation = new AddSpecificRegatta(regattaName, boatClassName, startDate, endDate, regattaId,
                regattaCreationParametersDTO, isPersistent, scoringScheme, courseAreaId, buoyZoneRadiusInHullLengths,
                useStartTimeInterference, controlTrackingFromStartAndFinishTimes, rankingMetric);

        Regatta regatta = getService().apply(operation);
        addLeaderboard(regatta.getName(), new ArrayList<Integer>());

        return ok(regatta.getId().toString(), MediaType.TEXT_PLAIN);
    }

    private CourseArea getDefaultCourseArea(Event event) {
        return stream(event.getVenue().getCourseAreas().spliterator())
                .filter(c -> c.getName().equals(DEFAULT_COURSE_AREA_NAME)).findFirst().orElse(null);
    }

    @POST
    @Path("/addCourseArea")
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response addCourseArea(@FormParam("eventId") String eventIdParam,
            @FormParam("courseAreaName") @DefaultValue(DEFAULT_SERIES_NAME) String courseAreaName) {

        if (eventIdParam == null) {
            return getParameterMissingResponse("eventId");
        }

        if (courseAreaName == null) {
            return getParameterMissingResponse("courseAreaName");
        }

        UUID eventId = null;
        try {
            eventId = UUID.fromString(eventIdParam);
        } catch (IllegalArgumentException e) {
            return getInvalidIdFormatResponse(eventIdParam);
        }

        String[] courseAreaNames = new String[] { courseAreaName };
        UUID[] courseAreaIds = new UUID[] { UUID.randomUUID() };

        CourseArea[] courseAreas = null;
        CourseArea result = null;
        try {
            courseAreas = getService().apply(new AddCourseAreas(eventId, courseAreaNames, courseAreaIds));
            result = courseAreas[0];
        } catch (IllegalArgumentException e) {
            return getBadEventErrorResponse(eventId.toString());
        }

        return  ok(result.getId().toString(), MediaType.TEXT_PLAIN);
    }

    @POST
    @Path("/addLeaderboard")
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response addLeaderboard(@FormParam("regattaName") String regattaName,
            @FormParam("discardThreshold") List<Integer> discardThresholdsParam) {

        if (regattaName == null) {
            return getParameterMissingResponse("regattaName");
        }

        if (getService().getRegatta(new RegattaName(regattaName)) == null) {
            return getBadObjectErrorResponse(Regatta.class.getSimpleName(), regattaName);
        }

        int[] discardThresholds = discardThresholdsParam != null
                ? discardThresholdsParam.stream().mapToInt(i -> i).toArray() : null;

        final RegattaLeaderboard leaderboard = getService()
                .apply(new CreateRegattaLeaderboard(new RegattaName(regattaName), regattaName, discardThresholds));
        
        if(leaderboard == null){
            return getObjectAlreadyExistsResponse(RegattaLeaderboard.class, regattaName);
        }

        // searching for default leadboardgroup of event
        // how to do this easier?
        LeaderboardGroup foundLg = null;
        for (Event event : getService().getAllEvents()) {
            Iterable<CourseArea> courseAreas = event.getVenue().getCourseAreas();
            for (CourseArea courseArea : courseAreas) {
                if (courseArea.getId().equals(leaderboard.getRegatta().getDefaultCourseArea().getId())) {
                    for (LeaderboardGroup lg : event.getLeaderboardGroups()) {
                        // if default leaderboard
                        if (lg.getName().equals(event.getName())) {
                            foundLg = lg;
                            lg.addLeaderboard(leaderboard);
                        }
                    }
                }
            }
        }

        // check if there is a default leaderboard group
        if (foundLg != null) {
            List<String> leaderboards = stream(foundLg.getLeaderboards().spliterator()).map(lg -> lg.getName())
                    .collect(Collectors.toList());
            // how to get overallLeaderboard[...] parameters?
            getService().updateLeaderboardGroup(foundLg.getName(), foundLg.getName(), foundLg.getDescription(),
                    foundLg.getDisplayName(), leaderboards, null, null);
        }

        return ok(leaderboard.getName(), MediaType.TEXT_PLAIN);
    }

    private <T> Stream<T> stream(Spliterator<T> spliterator) {
        return StreamSupport.stream(spliterator, false);
    }

    @POST
    @Path("/addLeaderboardGroup")
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response addLeaderboardGroup(@FormParam("eventId") @NotNull String eventIdParam,
            @FormParam("leaderboardGroupName") String leaderboardGroupName,
            @FormParam("leaderboardGroupDescription") String leaderboardGroupDescription,
            @FormParam("leaderboardGroupDisplayName") String leaderboardGroupDisplayName,
            @FormParam("displayGroupsInReverseOrder") String displayGroupsInReverseOrderParam,
            @FormParam("leaderboardNames") List<String> leaderboardNames,
            @FormParam("overallLeaderboardDiscardThresholds") List<Integer> overallLeaderboardDiscardThresholdsParam,
            @FormParam("overallLeaderboardScoringSchemeType") String overallLeaderboardScoringSchemeTypeParam) {

        if (eventIdParam == null) {
            return getParameterMissingResponse("eventId");
        }

        if (leaderboardGroupDescription == null) {
            leaderboardGroupDescription = leaderboardGroupName;
        }
        
        boolean displayGroupsInReverseOrder = Boolean.parseBoolean(displayGroupsInReverseOrderParam);
        UUID eventId = null;
        try {
            eventId = UUID.fromString(eventIdParam);
        } catch (IllegalArgumentException e) {
            return getInvalidIdFormatResponse(eventIdParam);
        }

        Event event = getService().getEvent(eventId);
        if (event == null) {
            return getBadEventErrorResponse(eventId.toString());
        }

        if (leaderboardGroupName == null) {
            leaderboardGroupName = event.getName();
        }

        ScoringSchemeType overallLeaderboardScoringSchemeType = null;
        if (overallLeaderboardScoringSchemeTypeParam != null) {
            try {
                overallLeaderboardScoringSchemeType = ScoringSchemeType
                        .valueOf(overallLeaderboardScoringSchemeTypeParam);
            } catch (IllegalArgumentException e) {
                String correctValues = getEnumValuesAsString(ScoringSchemeType.class);
                return getValueUnknownResponse(overallLeaderboardScoringSchemeTypeParam, correctValues);
            }
        }

        int[] overallLeaderboardDiscardThresholds = overallLeaderboardDiscardThresholdsParam != null
                ? overallLeaderboardDiscardThresholdsParam.stream().mapToInt(i -> i).toArray() : new int[0];

        LeaderboardGroup leaderboardGroup = null;
        try {
            leaderboardGroup = getService().apply(new CreateLeaderboardGroup(leaderboardGroupName,
                    leaderboardGroupDescription, leaderboardGroupDisplayName, displayGroupsInReverseOrder,
                    leaderboardNames, overallLeaderboardDiscardThresholds, overallLeaderboardScoringSchemeType));
        } catch (IllegalArgumentException e) {
            return getObjectAlreadyExistsResponse(Leaderboard.class, leaderboardGroupName);
        }
       
        List<UUID> newLeaderboardGroupIds = new ArrayList<>();
        StreamSupport.stream(event.getLeaderboardGroups().spliterator(), false)
                .forEach(lg -> newLeaderboardGroupIds.add(lg.getId()));
        newLeaderboardGroupIds.add(leaderboardGroup.getId());

        getService().updateEvent(event.getId(), event.getName(), event.getDescription(), event.getStartDate(),
                event.getEndDate(), event.getVenue().getName(), event.isPublic(), newLeaderboardGroupIds,
                event.getOfficialWebsiteURL(), event.getBaseURL(), event.getSailorsInfoWebsiteURLs(), event.getImages(),
                event.getVideos());

        return ok(leaderboardGroup.getId().toString(), MediaType.TEXT_PLAIN);
    }

    private <E extends Enum<E>> String getEnumValuesAsString(Class<E> e) {
        return EnumSet.allOf(e).stream().map(en -> en.name()).collect(Collectors.joining(", "));
    }

    private LinkedHashMap<String, SeriesCreationParametersDTO> createDefaultSeriesCreationParameters() {
        final LinkedHashMap<String, SeriesCreationParametersDTO> seriesCreationParameters = new LinkedHashMap<>();
        seriesCreationParameters.put(DEFAULT_SERIES_NAME, new SeriesCreationParametersDTO(
                Arrays.asList(new FleetDTO(DEFAULT_FLEET_NAME, 0, null)), false, false, false, false, null, false, null));
        return seriesCreationParameters;
    }

    private Response ok(String message, String mediaType) {
        return Response.ok(message).header("Content-Type", mediaType + ";charset=UTF-8").build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getEvents(@QueryParam("showNonPublic") String showNonPublic) {
        // TODO bug2589, bug3504: the following will require EVENT:READ permission; it requires cross-server links to be
        // authentication aware...
        // SecurityUtils.getSubject().checkPermission(Permission.EVENT.getStringPermission(Permission.Mode.READ));
        JsonSerializer<EventBase> eventSerializer = new EventBaseJsonSerializer(
                new VenueJsonSerializer(new CourseAreaJsonSerializer()), new LeaderboardGroupBaseJsonSerializer());
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
        // TODO bug2589, bug3504: the following will require EVENT:READ permission; it requires cross-server links to be
        // authentication aware...
        // SecurityUtils.getSubject().checkPermission(Permission.EVENT.getStringPermissionForObjects(Permission.Mode.READ,
        // eventId));
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
            JsonSerializer<EventBase> eventSerializer = new EventBaseJsonSerializer(
                    new VenueJsonSerializer(new CourseAreaJsonSerializer()), new LeaderboardGroupBaseJsonSerializer());
            JSONObject eventJson = eventSerializer.serialize(event);

            String json = eventJson.toJSONString();
            response = Response.ok(json).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
        }
        return response;
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{eventId}/racestates")
    public Response getRaceStates(@PathParam("eventId") String eventId,
            @QueryParam("filterByLeaderboard") String filterByLeaderboard,
            @QueryParam("filterByCourseArea") String filterByCourseArea,
            @QueryParam("filterByDayOffset") String filterByDayOffset,
            @QueryParam("clientTimeZoneOffsetInMinutes") Integer clientTimeZoneOffsetInMinutes) {
        // TODO bug2589, bug3504: the following will require EVENT:READ permission; it requires cross-server links to be
        // authentication aware...
        // SecurityUtils.getSubject().checkPermission(Permission.EVENT.getStringPermissionForObjects(Permission.Mode.READ,
        // eventId));
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
                    clientTimeZoneOffset = new MillisecondsDurationImpl(1000 * 60 * clientTimeZoneOffsetInMinutes);
                } else {
                    clientTimeZoneOffset = Duration.NULL;
                }
            } else {
                clientTimeZoneOffset = null;
            }
            EventRaceStatesSerializer eventRaceStatesSerializer = new EventRaceStatesSerializer(filterByCourseArea,
                    filterByLeaderboard, filterByDayOffset, clientTimeZoneOffset, getService());
            JSONObject raceStatesJson = eventRaceStatesSerializer
                    .serialize(new Pair<Event, Iterable<Leaderboard>>(event, getService().getLeaderboards().values()));
            String json = raceStatesJson.toJSONString();
            response = Response.ok(json).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
        }
        return response;
    }

    private Response getBadEventErrorResponse(String eventId) {
        return Response.status(Status.NOT_FOUND)
                .entity("Could not find an event with id '" + StringEscapeUtils.escapeHtml(eventId) + "'.")
                .type(MediaType.TEXT_PLAIN).build();
    }

    private Response getBadObjectErrorResponse(String className, String id) {
        return Response.status(Status.NOT_FOUND).entity("Could not find object of type " + className
                + " with identifier '" + StringEscapeUtils.escapeHtml(id) + "'.").type(MediaType.TEXT_PLAIN).build();
    }

    private Response getObjectAlreadyExistsResponse(Object object, String id) {
        return Response
                .status(Status.BAD_REQUEST).entity("The object of type " + object.getClass().getSimpleName()
                        + " with id '" + StringEscapeUtils.escapeHtml(id) + "' already exists.")
                .type(MediaType.TEXT_PLAIN).build();
    }

    private Response getParameterMissingResponse(String parameter) {
        return Response.status(Status.BAD_REQUEST)
                .entity("The parameter " + StringEscapeUtils.escapeHtml(parameter) + " is required.")
                .type(MediaType.TEXT_PLAIN).build();
    }

    private Response getInvalidDateFormatResponse(String date) {
        return Response.status(Status.BAD_REQUEST).entity(
                "The date " + StringEscapeUtils.escapeHtml(date) + " does not follow the date pattern \"dd-MM-yyyy\".")
                .type(MediaType.TEXT_PLAIN).build();
    }

    private Response getInvalidURLFormatResponse(String url) {
        return Response.status(Status.BAD_REQUEST)
                .entity("The format of the url " + StringEscapeUtils.escapeHtml(url) + " is incorrect.")
                .type(MediaType.TEXT_PLAIN).build();
    }

    private Response getInvalidIdFormatResponse(String id) {
        return Response.status(Status.BAD_REQUEST)
                .entity("The format of the id " + StringEscapeUtils.escapeHtml(id) + " is incorrect.")
                .type(MediaType.TEXT_PLAIN).build();
    }

    private Response getValueUnknownResponse(String value, String correctValues) {
        return Response.status(Status.BAD_REQUEST)
                .entity("The value \"" + StringEscapeUtils.escapeHtml(value)
                        + "\" is not recognized. Correct values are: " + correctValues)
                .type(MediaType.TEXT_PLAIN).build();
    }

    private Response getLeaderboardNotFoundResponse(List<String> leaderboardNames) {
        return Response.status(Status.BAD_REQUEST)
                .entity("One of the following leaderboards was not found: \""
                        + StringEscapeUtils.escapeHtml(leaderboardNames.toString()) + "\" was not found.")
                .type(MediaType.TEXT_PLAIN).build();
    }

    private Response getMissingParametersResponse(List<String> parameters) {
        return Response.status(Status.BAD_REQUEST)
                .entity("At least one of the following parameters must be provided: "
                        + parameters.stream().collect(Collectors.joining(", ")) + "\" was not found.")
                .type(MediaType.TEXT_PLAIN).build();
    }

    private Date addOneWeek(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.WEEK_OF_MONTH, 1);
        return c.getTime();
    }
}
