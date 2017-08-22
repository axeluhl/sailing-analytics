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

import javax.ws.rs.Consumes;
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

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.NotFoundException;
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
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.jaxrs.exceptions.ExceptionManager;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseAreaJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.EventBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.EventRaceStatesSerializer;
import com.sap.sailing.server.gateway.serialization.impl.LeaderboardGroupBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.VenueJsonSerializer;
import com.sap.sailing.server.operationaltransformation.AddColumnToSeries;
import com.sap.sailing.server.operationaltransformation.AddCourseAreas;
import com.sap.sailing.server.operationaltransformation.AddSpecificRegatta;
import com.sap.sailing.server.operationaltransformation.CreateEvent;
import com.sap.sailing.server.operationaltransformation.CreateRegattaLeaderboard;
import com.sap.sailing.server.operationaltransformation.UpdateSeries;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.User;
import com.sap.sse.shared.media.ImageDescriptor;
import com.sap.sse.shared.media.VideoDescriptor;
import com.sap.sse.util.ServiceTrackerFactory;

@Path("/v1/events")
public class EventsResource extends AbstractSailingServerResource {
        
    private static final SimpleDateFormat shortDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private static final SimpleDateFormat longDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.S");
    
    private static final boolean DEBUG_SECURITY_CHECK = false;
    
    @POST
    @Path("/createEvent")
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response createEvent(@FormParam("eventName") String eventNameParam,
            @FormParam("eventDescription") String eventDescriptionParam,
            @FormParam("startDate") String startDateParam,
            @FormParam("endDate") String endDateParam, 
            @FormParam("venueName") String venueNameParam,
            @FormParam("isPublic") String isPublicParam,
            @FormParam("officialWebsiteURL") String officialWebsiteURLParam,
            @FormParam("baseURL")String baseURLParam,
            @FormParam("leaderboardGroupIds") List<String> leaderboardGroupIdsListParam,
            @FormParam("createLeaderboardGroup") String createLeaderboardGroupParam,
            @FormParam("createRegatta") String createRegattaParam,
            @FormParam("boatClassName") String boatClassNameParam) throws MalformedURLException, ParseException, NotFoundException
    {
        if(DEBUG_SECURITY_CHECK) SecurityUtils.getSubject().checkPermission(Permission.EVENT.getStringPermission(Mode.CREATE));
        
        Event event = validateAndCreateEvent(eventNameParam, eventDescriptionParam, startDateParam, endDateParam, venueNameParam,
                isPublicParam, officialWebsiteURLParam, baseURLParam, leaderboardGroupIdsListParam,
                createLeaderboardGroupParam, createRegattaParam, boatClassNameParam);
        
         return ok(event.getId().toString(), MediaType.TEXT_PLAIN);
    }
    
    @POST
    @Path("/addRegatta")
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response addRegatta(@FormParam("regattaName") String regattaNameParam,
            @FormParam("boatClassName") String boatClassNameParam,
            @FormParam("startDate") String startDateParam, 
            @FormParam("eventId") String eventIdParam,
            @FormParam("endDate") String endDateParam,
            @FormParam("persistent") String isPersistentParam,
            @FormParam("scoringScheme") String scoringSchemeParam,
            @FormParam("courseAreaId") String courseAreaIdParam,
            @FormParam("buoyZoneRadiusInHullLengths") String buoyZoneRadiusInHullLengthsParam,
            @FormParam("useStartTimeInterference") String useStartTimeInterferenceParam,
            @FormParam("controlTrackingFromStartAndFinishTimes") String controlTrackingFromStartAndFinishTimesParam,
            @FormParam("rankingMetric") String rankingMetricParam,
            @FormParam("leaderboardDiscardThresholds") List<Integer> leaderboardDiscardThresholdsParam,
            @FormParam("numberOfRaces")  String numberOfRacesParam) throws ParseException, NotFoundException
    {
        if(DEBUG_SECURITY_CHECK) SecurityUtils.getSubject().checkPermission(Permission.REGATTA.getStringPermissionForObjects(Mode.CREATE, regattaNameParam));
        if(DEBUG_SECURITY_CHECK) SecurityUtils.getSubject().checkPermission(Permission.EVENT.getStringPermission(Mode.READ));
        
        Regatta regatta = validateAndCreateRegatta(regattaNameParam, boatClassNameParam, startDateParam, eventIdParam, endDateParam,
                isPersistentParam, scoringSchemeParam, courseAreaIdParam, buoyZoneRadiusInHullLengthsParam,
                useStartTimeInterferenceParam, controlTrackingFromStartAndFinishTimesParam, rankingMetricParam, leaderboardDiscardThresholdsParam, numberOfRacesParam);

        return ok(regatta.getId().toString(), MediaType.TEXT_PLAIN);
    }

    @POST
    @Path("/addCourseArea")
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response addCourseArea(@FormParam("eventId") String eventIdParam,
            @FormParam("courseAreaName") String courseAreaNameParam) 
    {
        if(DEBUG_SECURITY_CHECK) SecurityUtils.getSubject().checkPermission(Permission.EVENT.getStringPermission(Mode.CREATE));
        
        if(eventIdParam == null) throw new IllegalArgumentException(ExceptionManager.parameterRequiredMsg("eventId"));
        if(courseAreaNameParam == null) throw new IllegalArgumentException(ExceptionManager.parameterRequiredMsg("courseAreaName"));
        
        UUID eventId = toUUID(eventIdParam);
        CourseArea courseArea = addCourseArea(eventId, courseAreaNameParam);

        return  ok(courseArea.getId().toString(), MediaType.TEXT_PLAIN);
    }

    @POST
    @Path("/addLeaderboard")
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response addLeaderboard(@FormParam("regattaName") String regattaNameParam,
            @FormParam("discardThreshold") List<Integer> discardThresholdsParam) 
    {
        if(DEBUG_SECURITY_CHECK) SecurityUtils.getSubject().checkPermission(Permission.LEADERBOARD.getStringPermissionForObjects(Mode.CREATE, regattaNameParam));
        
        if(regattaNameParam == null) throw new IllegalArgumentException(ExceptionManager.parameterRequiredMsg("regattaName"));
        
        int[] discardThresholds = discardThresholdsParam == null ? new int[0] : discardThresholdsParam.stream().mapToInt(i -> i).toArray();

        RegattaLeaderboard leaderboard = addLeaderboard(regattaNameParam, discardThresholds);

        return ok(leaderboard.getName(), MediaType.TEXT_PLAIN);
    }

    @POST
    @Path("/addLeaderboardGroup")
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response addLeaderboardGroup(@FormParam("eventId") String eventIdParam,
            @FormParam("leaderboardGroupName") String leaderboardGroupNameParam,
            @FormParam("leaderboardGroupDescription") String leaderboardGroupDescriptionParam,
            @FormParam("leaderboardGroupDisplayName") String leaderboardGroupDisplayNameParam,
            @FormParam("displayGroupsInReverseOrder") String displayGroupsInReverseOrderParam,
            @FormParam("leaderboardNames") List<String> leaderboardNamesParam,
            @FormParam("overallLeaderboardDiscardThresholds") List<Integer> overallLeaderboardDiscardThresholdsParam,
            @FormParam("overallLeaderboardScoringSchemeType") String overallLeaderboardScoringSchemeTypeParam) throws NotFoundException 
    {
        if(DEBUG_SECURITY_CHECK) SecurityUtils.getSubject().checkPermission(Permission.LEADERBOARD_GROUP.getStringPermissionForObjects(Mode.CREATE, leaderboardGroupNameParam));
        
        LeaderboardGroup leaderboardGroup = validateAndAddLeaderboardGroup(eventIdParam, leaderboardGroupNameParam,
                leaderboardGroupDescriptionParam, leaderboardGroupDescriptionParam, displayGroupsInReverseOrderParam,
                leaderboardNamesParam, overallLeaderboardDiscardThresholdsParam,
                overallLeaderboardScoringSchemeTypeParam);
        
        return ok(leaderboardGroup.getId().toString(), MediaType.TEXT_PLAIN);
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
            eventUuid = toUUID(eventId);
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
            eventUuid = toUUID(eventId);
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
    
    private Regatta validateAndCreateRegatta(String regattaNameParam, String boatClassNameParam, String startDateParam, 
            String eventIdParam, String endDateParam, String isPersistentParam, String scoringSchemeParam, String courseAreaIdParam, String buoyZoneRadiusInHullLengthsParam,
            String useStartTimeInterferenceParam, String controlTrackingFromStartAndFinishTimesParam, String rankingMetricParam, List<Integer> leaderboardDiscardThresholdsParam, String numberOfRacesParam) throws ParseException, NotFoundException{
        
        boolean isPersistent = isPersistentParam == null ? true : Boolean.parseBoolean(isPersistentParam);
        boolean controlTrackingFromStartAndFinishTimes = controlTrackingFromStartAndFinishTimesParam == null ? false : Boolean.parseBoolean(controlTrackingFromStartAndFinishTimesParam);
        boolean useStartTimeInterference = useStartTimeInterferenceParam == null ? true : Boolean.parseBoolean(useStartTimeInterferenceParam);
        double buoyZoneRadiusInHullLengths = buoyZoneRadiusInHullLengthsParam == null ? 3.0 : Double.parseDouble(buoyZoneRadiusInHullLengthsParam);
        
        if (regattaNameParam == null) throw new IllegalArgumentException(ExceptionManager.parameterRequiredMsg("regattaName"));
        if (boatClassNameParam == null) throw new IllegalArgumentException(ExceptionManager.parameterRequiredMsg("boatClassName"));
        if (eventIdParam == null && courseAreaIdParam == null) throw new IllegalArgumentException(ExceptionManager.atLeastOneParameterRequiredMsg("eventId", "courseAreaId"));
        
        String regattaName = regattaNameParam;
        UUID eventId = eventIdParam == null ? null : toUUID(eventIdParam);
        UUID courseAreaId = courseAreaIdParam == null ? null : toUUID(courseAreaIdParam);
        String boatClassName = boatClassNameParam;
        MillisecondsTimePoint startDate = startDateParam == null ? null :  toMillisecondsTimePoint(startDateParam);
        MillisecondsTimePoint endDate = endDateParam == null ? null : toMillisecondsTimePoint(endDateParam) ;
        ScoringScheme scoringScheme = scoringSchemeParam == null ? createScoringScheme("LOW_POINT") : createScoringScheme(scoringSchemeParam);
        RankingMetrics rankingMetric = rankingMetricParam == null ? createRankingMetric("ONE_DESIGN") : createRankingMetric(rankingMetricParam);
        int[] leaderboardDiscardThresholds = leaderboardDiscardThresholdsParam == null ? new int[0] : leaderboardDiscardThresholdsParam.stream().mapToInt(i -> i).toArray();
        int numberOfRaces = numberOfRacesParam == null ? 3 : Integer.parseInt(numberOfRacesParam);
        RegattaCreationParametersDTO regattaCreationParametersDTO = new RegattaCreationParametersDTO(
                createDefaultSeriesCreationParameters(regattaName, numberOfRaces));
        UUID regattaId = UUID.randomUUID();
        
        // if courseAreaId not provided then get it from eventId
        if (eventId != null && courseAreaId == null) courseAreaId = getDefaultCourseArea(getEvent(eventId)).getId();
    
        Regatta regatta = addRegatta(regattaName, isPersistent, controlTrackingFromStartAndFinishTimes,
                useStartTimeInterference, buoyZoneRadiusInHullLengths, courseAreaId, boatClassName, startDate, endDate,
                scoringScheme, rankingMetric, regattaId, regattaCreationParametersDTO, leaderboardDiscardThresholds, numberOfRaces);
        
        addLeaderboard(regattaName, leaderboardDiscardThresholds);
        
        SeriesCreationParametersDTO defaultSeries = regattaCreationParametersDTO.getSeriesCreationParameters().get("Default");
        addRaceColumns(regattaName, "Default", numberOfRaces, boatClassName);
        updateSeries(regattaName, defaultSeries);
        
        return regatta; 
    }

    private Event validateAndCreateEvent(String eventNameParam, String eventDescriptionParam, String startDateParam, String endDateParam, String venueNameParam,
            String isPublicParam, String officialWebsiteURLParam, String baseURLParam, List<String >leaderboardGroupIdsListParam,
            String createLeaderboardGroupParam, String createRegattaParam, String boatClassNameParam) throws ParseException, MalformedURLException, NotFoundException{
        
        boolean isPublic = isPublicParam == null ? false : Boolean.parseBoolean(isPublicParam);
        boolean createRegatta = createRegattaParam == null ? true : Boolean.parseBoolean(createRegattaParam);
        boolean createLeaderboardGroup = createLeaderboardGroupParam == null ? true : Boolean.parseBoolean(createLeaderboardGroupParam);
        
//        String eventName = eventNameParam == null ? getDefaultEventName(SecurityUtils.getSubject()) : eventNameParam;
        String eventName = eventNameParam == null ? randomName() : eventNameParam;
        
        String venueName = venueNameParam == null ? getDefaultVenueName("", "") : venueNameParam;
        if (createRegatta && boatClassNameParam == null) {
            throw new IllegalArgumentException(ExceptionManager.parameterRequiredMsg("boatClassName"));
        }

        String boatClassName = boatClassNameParam == null ? null : getBoatClassDisplayName(boatClassNameParam);
        String eventDescription = eventDescriptionParam == null ? eventName : eventDescriptionParam;
        MillisecondsTimePoint startDate = startDateParam == null ? now() :  toMillisecondsTimePoint(startDateParam);
        MillisecondsTimePoint endDate = endDateParam == null ? new MillisecondsTimePoint(addOneWeek(startDate.asDate())): toMillisecondsTimePoint(endDateParam) ;
        URL officialWebsiteURL = officialWebsiteURLParam == null ? null :  toURL(officialWebsiteURLParam);
        URL baseURL = baseURLParam == null ? null : toURL(baseURLParam);
        List<UUID> leaderboardGroupIds = leaderboardGroupIdsListParam == null ? new ArrayList<UUID>() : toUUIDList(leaderboardGroupIdsListParam);
        UUID eventId = UUID.randomUUID();
    
        // ignoring sailorsInfoWebsiteURLs, images, videos
        Map<Locale, URL> sailorsInfoWebsiteURLs = new HashMap<Locale,URL>();
        Iterable<ImageDescriptor> images = Collections.<ImageDescriptor> emptyList();
        Iterable<VideoDescriptor> videos = Collections.<VideoDescriptor> emptyList();
        
        Event event = createEvent(eventName, eventDescription, venueName, isPublic, startDate, endDate, officialWebsiteURL, baseURL,
                leaderboardGroupIds, eventId, sailorsInfoWebsiteURLs, images, videos);
    
        CourseArea courseArea = addCourseArea(event.getId(), "Default");
    
        if (createLeaderboardGroup) {
            addLeaderboardGroup(event.getId().toString(), event.getName(), event.getName(), null, null, null, null, null);
        }
    
        if (createRegatta) {
            addRegatta(event.getName(), boatClassName, null, null, null, null, null,
                    courseArea.getId().toString(), null, null, null, null, null, null);
        }
        
        return event;
    }

    private LeaderboardGroup validateAndAddLeaderboardGroup(String eventIdParam, String leaderboardGroupNameParam,
            String leaderboardGroupDescriptionParam, String leaderboardGroupDisplayNameParam, String displayGroupsInReverseOrderParam,
            List<String> leaderboardNamesParam, List<Integer> overallLeaderboardDiscardThresholdsParam, String overallLeaderboardScoringSchemeTypeParam) throws NotFoundException{
        
        if(eventIdParam == null) throw new IllegalArgumentException(ExceptionManager.parameterRequiredMsg("eventId"));
        
        UUID eventId = toUUID(eventIdParam);
        Event event = getEvent(eventId);
        boolean displayGroupsInReverseOrder = displayGroupsInReverseOrderParam == null ? false : Boolean.parseBoolean(displayGroupsInReverseOrderParam);
        String leaderboardGroupDescription = leaderboardGroupDescriptionParam == null ? leaderboardGroupNameParam : leaderboardGroupDescriptionParam;
        String leaderboardGroupDisplayName = leaderboardGroupDisplayNameParam == null ? null : leaderboardGroupDisplayNameParam;
        String leaderboardGroupName = leaderboardGroupNameParam == null ? event.getName() : leaderboardGroupNameParam;
        ScoringSchemeType overallLeaderboardScoringSchemeType = overallLeaderboardScoringSchemeTypeParam == null ? getScoringSchemeType("LOW_POINT") : getScoringSchemeType(overallLeaderboardScoringSchemeTypeParam); 
        int[] overallLeaderboardDiscardThresholds = overallLeaderboardDiscardThresholdsParam == null ? new int[0] : overallLeaderboardDiscardThresholdsParam.stream().mapToInt(i -> i).toArray();
        List<String> leaderboardNames = leaderboardNamesParam == null ? new ArrayList<String>() : leaderboardNamesParam;
        
        LeaderboardGroup leaderboardGroup = getService().addLeaderboardGroup(event.getId(), leaderboardGroupName, leaderboardGroupDescription, leaderboardGroupDisplayName, displayGroupsInReverseOrder, leaderboardNames, overallLeaderboardDiscardThresholds, overallLeaderboardScoringSchemeType);
        updateEvent(event, leaderboardGroup);
        
        return leaderboardGroup;
    }

    private String getDefaultEventName(Subject subject){
//        User user = getCurrentUser();    
//        return DEBUG_SECURITY_CHECK && user == null ? RandomStringUtils.randomAlphanumeric(6).toUpperCase() : user.getName();
        
        return randomName();
    }

    private String randomName() {
        return RandomStringUtils.randomAlphanumeric(6).toUpperCase();
    }

    private User getCurrentUser() {
        User user = getSecurityService().getCurrentUser();
        return user;
    }

    private SecurityService getSecurityService() {
        BundleContext context = FrameworkUtil.getBundle(EventsResource.class).getBundleContext();
        return ServiceTrackerFactory.createAndOpen(context, SecurityService.class).getService();
    }
    
    private String getDefaultVenueName(String lat, String lng){
        return randomName();
    }

    private void updateSeries(String regattaName, SeriesCreationParametersDTO defaultSeries) {
        getService().apply(new UpdateSeries(new RegattaName(regattaName), "Default", "Default", defaultSeries.isMedal(),
                defaultSeries.isFleetsCanRunInParallel(), defaultSeries.getDiscardingThresholds(),
                defaultSeries.isStartsWithZero(), defaultSeries.isFirstColumnIsNonDiscardableCarryForward(),
                defaultSeries.hasSplitFleetContiguousScoring(), defaultSeries.getMaximumNumberOfDiscards(),
                defaultSeries.getFleets()));
    }
    
    private void addRaceColumns(String regattaName, String seriesName, int numberOfRaces, String boatClassName) {
        for (int i = 1; i <= numberOfRaces; i++) {
            addRaceColumn(regattaName, seriesName, getDefaultRaceName(boatClassName));
        }
    }

    private Event createEvent(String eventName, String eventDescription, String venueName, boolean isPublic,
            MillisecondsTimePoint startDate, MillisecondsTimePoint endDate, URL officialWebsiteURL, URL baseURL,
            List<UUID> leaderboardGroupIds, UUID eventId, Map<Locale, URL> sailorsInfoWebsiteURLs,
            Iterable<ImageDescriptor> images, Iterable<VideoDescriptor> videos) {
        return getService()
                .apply(new CreateEvent(eventName, eventDescription, startDate, endDate, venueName, isPublic, eventId,
                        officialWebsiteURL, baseURL, sailorsInfoWebsiteURLs, images, videos, leaderboardGroupIds));
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

    private CourseArea addCourseArea(UUID eventId, String courseAreaName) {
        String[] courseAreaNames = new String[] { courseAreaName };
        UUID[] courseAreaIds = new UUID[] { UUID.randomUUID() };
        return getService().apply(new AddCourseAreas(eventId, courseAreaNames, courseAreaIds))[0];
    }

    private Regatta addRegatta(String regattaName, boolean isPersistent, boolean controlTrackingFromStartAndFinishTimes,
            boolean useStartTimeInterference, double buoyZoneRadiusInHullLengths, UUID courseAreaId,
            String boatClassName, MillisecondsTimePoint startDate, MillisecondsTimePoint endDate,
            ScoringScheme scoringScheme, RankingMetrics rankingMetric, UUID regattaId,
            RegattaCreationParametersDTO regattaCreationParametersDTO, int[] leaderboardDiscardThresholds, int numberOfRaces) {
        
        Regatta regatta = getService().apply(new AddSpecificRegatta(regattaName, boatClassName, startDate, endDate, regattaId,
                regattaCreationParametersDTO, isPersistent, scoringScheme, courseAreaId, buoyZoneRadiusInHullLengths,
                useStartTimeInterference, controlTrackingFromStartAndFinishTimes, rankingMetric));

        return regatta;
    }
    
    private RaceColumnInSeries addRaceColumn(String regattaName, String seriesName, String columnName){
        return getService().apply(new AddColumnToSeries(new RegattaName(regattaName),seriesName, columnName));
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
            
            ResultDiscardingRule rule = defaultLeaderboardGroup.getOverallLeaderboard().getResultDiscardingRule();
            int[] overallLeaderboardDiscardThresholds = null; 
            if(rule instanceof ThresholdBasedResultDiscardingRule){
                ThresholdBasedResultDiscardingRule resultDiscardingRule = (ThresholdBasedResultDiscardingRule) rule;
                overallLeaderboardDiscardThresholds = resultDiscardingRule.getDiscardIndexResultsStartingWithHowManyRaces();
            }
            
            getService().updateLeaderboardGroup(defaultLeaderboardGroup.getName(), defaultLeaderboardGroup.getName(), defaultLeaderboardGroup.getDescription(),
                    defaultLeaderboardGroup.getDisplayName(), leaderboards, overallLeaderboardDiscardThresholds, defaultLeaderboardGroup.getOverallLeaderboard().getScoringScheme().getType());
        }
    }
    
    private String getDefaultRaceName(String boatClassName){
//        User user = getCurrentUser();
//        return user != null ? user.getName() + " " + longDateFormat.format(new Date()) + " " + boatClassName : randomName();
        return randomName();
    }

    private RegattaLeaderboard addLeaderboard(String regattaName, int[] discardThresholds) {
        RegattaLeaderboard leaderboard = null;
        try {
            leaderboard = createRegattaLeaderboard(regattaName, discardThresholds);
        } catch (IllegalArgumentException e) {
            throw e;
        }
        
        addLeaderboardToDefaultLeaderboardGroup(leaderboard);
        return leaderboard;
    }

    private CourseArea getDefaultCourseArea(Event event) {
        return stream(event.getVenue().getCourseAreas().spliterator())
                .filter(c -> c.getName().equals("Default")).findFirst().orElse(null);
    }
    
    
    private RegattaLeaderboard createRegattaLeaderboard(String regattaName, int[] discardThresholds) {
        return getService()
                .apply(new CreateRegattaLeaderboard(new RegattaName(regattaName), regattaName, discardThresholds));
    }

    private Response ok(String message, String mediaType) {
        return Response.ok(message).header("Content-Type", mediaType + ";charset=UTF-8").build();
    }

    private Response getBadEventErrorResponse(String eventId) {
        return Response.status(Status.NOT_FOUND)
                .entity("Could not find an event with id '" + StringEscapeUtils.escapeHtml(eventId) + "'.")
                .type(MediaType.TEXT_PLAIN).build();
    }

    private MillisecondsTimePoint now() {
        return new MillisecondsTimePoint(new Date());
    }

    private Date addOneWeek(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.WEEK_OF_MONTH, 1);
        return c.getTime();
    }

    private MillisecondsTimePoint toMillisecondsTimePoint(String startDateParam) throws ParseException {
        try {
            return new MillisecondsTimePoint(shortDateFormat.parse(startDateParam));
        } catch (ParseException e) {
            throw new ParseException(ExceptionManager.invalidDateFormatMsg(startDateParam),0);
        }
    }

    private URL toURL(String url) throws MalformedURLException{
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new MalformedURLException(ExceptionManager.invalidURLFormatMsg(url));
        }
    }
    
    private List<UUID> toUUIDList(List<String> list){
        return list.stream().map(id -> toUUID(id))
                .collect(Collectors.toList());
    }


    private UUID toUUID(String id) {
        try{
            return UUID.fromString(id);
        }
       catch(IllegalArgumentException e){
           throw new IllegalArgumentException(ExceptionManager.invalidIdFormatMsg(id));
       }
    }
    
    private LinkedHashMap<String, SeriesCreationParametersDTO> createDefaultSeriesCreationParameters(String regattaName,int numberOfRaces ) {
        final LinkedHashMap<String, SeriesCreationParametersDTO> series = new LinkedHashMap<>();

        series.put("Default", new SeriesCreationParametersDTO(
                Arrays.asList(new FleetDTO("Default", 0, null)), false, false, false, false, null, false, null));

        return series;
    }

    private RankingMetrics createRankingMetric(String rankingMetricParam) {
        try{
            return RankingMetrics.valueOf(rankingMetricParam);
        }
        catch(IllegalArgumentException e){
            throw new IllegalArgumentException(ExceptionManager.incorrectParameterValue(rankingMetricParam, getEnumValuesAsString(RankingMetrics.class)));
        }
    }


    private ScoringScheme createScoringScheme(String scoringSchemeParam) {   
        ScoringScheme scoringScheme = getService().getBaseDomainFactory().createScoringScheme(getScoringSchemeType(scoringSchemeParam));
        return scoringScheme;
    }

    private <E extends Enum<E>> String getEnumValuesAsString(Class<E> e) {
        return EnumSet.allOf(e).stream().map(en -> en.name()).collect(Collectors.joining(", "));
    }

    private ScoringSchemeType getScoringSchemeType(String scoringSchemeTypeParam) {
        try{
            return ScoringSchemeType.valueOf(scoringSchemeTypeParam);
        }
        catch(IllegalArgumentException e){
            throw new IllegalArgumentException(ExceptionManager.incorrectParameterValue(scoringSchemeTypeParam, getEnumValuesAsString(ScoringSchemeType.class)));
        }
    }


    private String getBoatClassDisplayName(String boatClassNameParam) {
        try{
            return BoatClassMasterdata.valueOf(boatClassNameParam).getDisplayName();
        }
        catch(IllegalArgumentException e){
            throw new IllegalArgumentException(ExceptionManager.incorrectParameterValue(boatClassNameParam, getEnumValuesAsString(BoatClassMasterdata.class)));
        }
    }
    
    private Regatta getRegatta(String regattaNameParam) {
        return getService().getRegatta(new RegattaName(regattaNameParam));
    }

    private Event getEvent(UUID eventId) throws NotFoundException{
        Event event = getService().getEvent(eventId);
        if(event != null){
            return event;
        }
        throw new NotFoundException(ExceptionManager.objectNotFoundMsg(Event.class.getSimpleName(), eventId));
    }

    private <T> Stream<T> stream(Spliterator<T> spliterator) {
        return StreamSupport.stream(spliterator, false);
    }

}
