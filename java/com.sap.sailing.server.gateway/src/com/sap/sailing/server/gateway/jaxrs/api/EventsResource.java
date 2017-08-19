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
import java.util.Spliterator;
import java.util.UUID;
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
            @FormParam("isPublic") @DefaultValue("false") String isPublicParam,
            @FormParam("officialWebsiteURL") String officialWebsiteURLString,
            @FormParam("baseURL") String baseURLString,
            @FormParam("leaderboardGroupIds") List<String> leaderboardGroupIdsList,
            @FormParam("createLeaderboardGroup") @DefaultValue("false") String createLeaderboardGroupParam,
            @FormParam("createRegatta") @DefaultValue("false") String createRegattaParam,
            @FormParam("boatClassName") String boatClassName)
    {
//        SecurityUtils.getSubject().checkPermission(Permission.EVENT.getStringPermission(Mode.CREATE));

        boolean isPublic = Boolean.parseBoolean(isPublicParam);
        boolean createRegatta = Boolean.parseBoolean(createRegattaParam);
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
        
        List<UUID> leaderboardGroupIds = new ArrayList<UUID>();
        if(leaderboardGroupIdsList != null){
            leaderboardGroupIds = leaderboardGroupIdsList.stream().map(id -> UUID.fromString(id))
                    .collect(Collectors.toList());
        }


        UUID eventId = UUID.randomUUID();

        // ignoring sailorsInfoWebsiteURLs, images, videos
        Map<Locale, URL> sailorsInfoWebsiteURLs = new HashMap<Locale,URL>();
        Iterable<ImageDescriptor> images = Collections.<ImageDescriptor> emptyList();
        Iterable<VideoDescriptor> videos = Collections.<VideoDescriptor> emptyList();
        
        Event event = createEvent(eventName, eventDescription, venueName, isPublic, startDate, endDate, officialWebsiteURL, baseURL,
                leaderboardGroupIds, eventId, sailorsInfoWebsiteURLs, images, videos);

        CourseArea courseArea = addCourseArea(event.getId(), "Default");

        if (createLeaderboardGroup) {
            addLeaderboardGroup(event.getName(), event.getDescription(), null, new ArrayList<String>(), false, event, ScoringSchemeType.LOW_POINT, new int[0]);
        }

        if (createRegatta) {
            addRegatta(event.getName(), true, false, true,  3.0, courseArea.getId(), boatClassName, new MillisecondsTimePoint(new Date()), new MillisecondsTimePoint(addOneWeek(new Date())), getService().getBaseDomainFactory().createScoringScheme(ScoringSchemeType.LOW_POINT), RankingMetrics.ONE_DESIGN, UUID.randomUUID(), new RegattaCreationParametersDTO(
                createDefaultSeriesCreationParameters()));
            
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
            @FormParam("persistent") @DefaultValue("true") String isPersistentParam,
            @FormParam("scoringScheme") @DefaultValue("LOW_POINT") String scoringSchemeParam,
            @FormParam("courseAreaId") String courseAreaIdParam,
            @FormParam("buoyZoneRadiusInHullLengths") @DefaultValue("3.0") String buoyZoneRadiusInHullLengthsParam,
            @FormParam("useStartTimeInterference") @DefaultValue("true") String useStartTimeInterferenceParam,
            @FormParam("controlTrackingFromStartAndFinishTimes") @DefaultValue("false") String controlTrackingFromStartAndFinishTimesParam,
            @FormParam("rankingMetric") @DefaultValue("ONE_DESIGN") String rankingMetricParam)
        {
//        SecurityUtils.getSubject().checkPermission(Permission.REGATTA.getStringPermissionForObjects(Mode.CREATE, regattaName));
//        SecurityUtils.getSubject().checkPermission(Permission.EVENT.getStringPermission(Mode.READ));
        
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
            boatClassName = BoatClassMasterdata.valueOf(boatClassNameParam).getDisplayName();
        } catch (IllegalArgumentException e) {
            String correctValues = getBoatClassDisplayNames();
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

        Regatta regatta = addRegatta(regattaName, isPersistent, controlTrackingFromStartAndFinishTimes,
                useStartTimeInterference, buoyZoneRadiusInHullLengths, courseAreaId, boatClassName, startDate, endDate,
                scoringScheme, rankingMetric, regattaId, regattaCreationParametersDTO);
        
        addLeaderboard(regattaName, new int[0]);
       

        return ok(regatta.getId().toString(), MediaType.TEXT_PLAIN);
    }


    @POST
    @Path("/addCourseArea")
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response addCourseArea(@FormParam("eventId") String eventIdParam,
            @FormParam("courseAreaName") @DefaultValue("Default") String courseAreaName) {

//        SecurityUtils.getSubject().checkPermission(Permission.COURSE_AREA.getStringPermission(Mode.CREATE));
//        SecurityUtils.getSubject().checkPermission(Permission.EVENT.getStringPermission(Mode.CREATE));
        
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

        CourseArea result = addCourseArea(eventId, courseAreaName);

        return  ok(result.getId().toString(), MediaType.TEXT_PLAIN);
    }

    private CourseArea addCourseArea(UUID eventId, String courseAreaName) {
        String[] courseAreaNames = new String[] { courseAreaName };
        UUID[] courseAreaIds = new UUID[] { UUID.randomUUID() };
        CourseArea[] courseAreas = null;
        CourseArea result = null;
        try {
            courseAreas = getService().apply(new AddCourseAreas(eventId, courseAreaNames, courseAreaIds));
            result = courseAreas[0];
        } catch (IllegalArgumentException e) {
//            return getBadEventErrorResponse(eventId.toString());
        }
        return result;
    }

    @POST
    @Path("/addLeaderboard")
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response addLeaderboard(@FormParam("regattaName") String regattaName,
            @FormParam("discardThreshold") List<Integer> discardThresholdsParam) {

//        SecurityUtils.getSubject().checkPermission(Permission.LEADERBOARD.getStringPermissionForObjects(Mode.CREATE, regattaName));
        
        if (regattaName == null) {
            return getParameterMissingResponse("regattaName");
        }

        if (getService().getRegatta(new RegattaName(regattaName)) == null) {
            return getBadObjectErrorResponse(Regatta.class.getSimpleName(), regattaName);
        }

        int[] discardThresholds = discardThresholdsParam != null
                ? discardThresholdsParam.stream().mapToInt(i -> i).toArray() : null;

        final RegattaLeaderboard leaderboard = addLeaderboard(regattaName, discardThresholds);

        return ok(leaderboard.getName(), MediaType.TEXT_PLAIN);
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

//        SecurityUtils.getSubject().checkPermission(Permission.LEADERBOARD_GROUP.getStringPermissionForObjects(Mode.CREATE, leaderboardGroupName));
        
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

        LeaderboardGroup leaderboardGroup = addLeaderboardGroup(leaderboardGroupName, leaderboardGroupDescription,
                leaderboardGroupDisplayName, leaderboardNames, displayGroupsInReverseOrder, event,
                overallLeaderboardScoringSchemeType, overallLeaderboardDiscardThresholds);

        return ok(leaderboardGroup.getId().toString(), MediaType.TEXT_PLAIN);
    }

    
    private Event createEvent(String eventName, String eventDescription, String venueName, boolean isPublic,
            MillisecondsTimePoint startDate, MillisecondsTimePoint endDate, URL officialWebsiteURL, URL baseURL,
            List<UUID> leaderboardGroupIds, UUID eventId, Map<Locale, URL> sailorsInfoWebsiteURLs,
            Iterable<ImageDescriptor> images, Iterable<VideoDescriptor> videos) {
        return getService()
                .apply(new CreateEvent(eventName, eventDescription, startDate, endDate, venueName, isPublic, eventId,
                        officialWebsiteURL, baseURL, sailorsInfoWebsiteURLs, images, videos, leaderboardGroupIds));
    }


    private Regatta addRegatta(String regattaName, boolean isPersistent, boolean controlTrackingFromStartAndFinishTimes,
            boolean useStartTimeInterference, double buoyZoneRadiusInHullLengths, UUID courseAreaId,
            String boatClassName, MillisecondsTimePoint startDate, MillisecondsTimePoint endDate,
            ScoringScheme scoringScheme, RankingMetrics rankingMetric, UUID regattaId,
            RegattaCreationParametersDTO regattaCreationParametersDTO) {
        AddSpecificRegatta operation = new AddSpecificRegatta(regattaName, boatClassName, startDate, endDate, regattaId,
                regattaCreationParametersDTO, isPersistent, scoringScheme, courseAreaId, buoyZoneRadiusInHullLengths,
                useStartTimeInterference, controlTrackingFromStartAndFinishTimes, rankingMetric);

        Regatta regatta = getService().apply(operation);
        
        addLeaderboard(regattaName, new int[0]);
        return regatta;
    }

    private CourseArea getDefaultCourseArea(Event event) {
        return stream(event.getVenue().getCourseAreas().spliterator())
                .filter(c -> c.getName().equals("Default")).findFirst().orElse(null);
    }
    
    private LeaderboardGroup addLeaderboardGroup(String leaderboardGroupName, String leaderboardGroupDescription,
            String leaderboardGroupDisplayName, List<String> leaderboardNames, boolean displayGroupsInReverseOrder,
            Event event, ScoringSchemeType overallLeaderboardScoringSchemeType,
            int[] overallLeaderboardDiscardThresholds) {
        LeaderboardGroup leaderboardGroup = createLeaderboardGroup(leaderboardGroupName, leaderboardGroupDescription,
                leaderboardGroupDisplayName, leaderboardNames, displayGroupsInReverseOrder,
                overallLeaderboardScoringSchemeType, overallLeaderboardDiscardThresholds);
       
        updateEvent(event, leaderboardGroup);
        return leaderboardGroup;
    }

    private LeaderboardGroup createLeaderboardGroup(String leaderboardGroupName, String leaderboardGroupDescription,
            String leaderboardGroupDisplayName, List<String> leaderboardNames, boolean displayGroupsInReverseOrder,
            ScoringSchemeType overallLeaderboardScoringSchemeType, int[] overallLeaderboardDiscardThresholds) {
        LeaderboardGroup leaderboardGroup = null;
        try {
            leaderboardGroup = getService().apply(new CreateLeaderboardGroup(leaderboardGroupName,
                    leaderboardGroupDescription, leaderboardGroupDisplayName, displayGroupsInReverseOrder,
                    leaderboardNames, overallLeaderboardDiscardThresholds, overallLeaderboardScoringSchemeType));
        } catch (IllegalArgumentException e) {
            return null;
        }
        return leaderboardGroup;
    }
    
    private void updateEvent(Event event, LeaderboardGroup leaderboardGroup){
        List<UUID> newLeaderboardGroupIds = new ArrayList<>();
        StreamSupport.stream(event.getLeaderboardGroups().spliterator(), false)
                .forEach(lg -> newLeaderboardGroupIds.add(lg.getId()));
        newLeaderboardGroupIds.add(leaderboardGroup.getId());

        getService().updateEvent(event.getId(), event.getName(), event.getDescription(), event.getStartDate(),
                event.getEndDate(), event.getVenue().getName(), event.isPublic(), newLeaderboardGroupIds,
                event.getOfficialWebsiteURL(), event.getBaseURL(), event.getSailorsInfoWebsiteURLs(), event.getImages(),
                event.getVideos());
    }
    
    private RegattaLeaderboard addLeaderboard(String regattaName, int[] discardThresholds) {
        final RegattaLeaderboard leaderboard = createRegattaLeaderboard(regattaName, discardThresholds);
        
        if(leaderboard == null){
//            return getObjectAlreadyExistsResponse(RegattaLeaderboard.class, regattaName);
        }

        addLeaderboardToDefaultLeaderboardGroup(leaderboard);
        return leaderboard;
    }


    private RegattaLeaderboard createRegattaLeaderboard(String regattaName, int[] discardThresholds) {
        return getService()
                .apply(new CreateRegattaLeaderboard(new RegattaName(regattaName), regattaName, discardThresholds));
    }

    private void addLeaderboardToDefaultLeaderboardGroup(final RegattaLeaderboard leaderboard) {
        LeaderboardGroup defaultLeaderboardGroup = null;
        for (Event event : getService().getAllEvents()) {
            Iterable<CourseArea> courseAreas = event.getVenue().getCourseAreas();
            for (CourseArea courseArea : courseAreas) {
                if (courseArea.getId().equals(leaderboard.getRegatta().getDefaultCourseArea().getId())) {
                    for (LeaderboardGroup lg : event.getLeaderboardGroups()) {
                        // if leaderboard group is default leaderboard group, then add leaderboard
                        if (lg.getName().equals(event.getName())) {
                            defaultLeaderboardGroup = lg;
                        }
                    }
                }
            }
        }
        
        if (defaultLeaderboardGroup != null) {
            defaultLeaderboardGroup.addLeaderboard(leaderboard);
            List<String> leaderboards = stream(defaultLeaderboardGroup.getLeaderboards().spliterator()).map(lg -> lg.getName())
                    .collect(Collectors.toList());
            
            // how to get overallLeaderboard[...] parameters?
            getService().updateLeaderboardGroup(defaultLeaderboardGroup.getName(), defaultLeaderboardGroup.getName(), defaultLeaderboardGroup.getDescription(),
                    defaultLeaderboardGroup.getDisplayName(), leaderboards, null, null);
        }
    }

    private <T> Stream<T> stream(Spliterator<T> spliterator) {
        return StreamSupport.stream(spliterator, false);
    }


    private <E extends Enum<E>> String getEnumValuesAsString(Class<E> e) {
        return EnumSet.allOf(e).stream().map(en -> en.name()).collect(Collectors.joining(", "));
    }
    
    private String getBoatClassDisplayNames() {
        return EnumSet.allOf(BoatClassMasterdata.class).stream().map(en -> en.getDisplayName()).collect(Collectors.joining(", "));
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
