package com.sap.sailing.server.gateway.jaxrs.api;

import java.io.IOException;
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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.common.NotFoundException;
import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.dto.SeriesCreationParametersDTO;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.security.Permission;
import com.sap.sailing.domain.common.security.Permission.Mode;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.geocoding.ReverseGeocoder;
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
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardGroup;
import com.sap.sailing.server.operationaltransformation.UpdateSeries;
import com.sap.sse.InvalidDateException;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.User;
import com.sap.sse.shared.media.ImageDescriptor;
import com.sap.sse.shared.media.VideoDescriptor;
import com.sap.sse.util.ServiceTrackerFactory;

@Path("/v1/events")
public class EventsResource extends AbstractSailingServerResource {
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private final boolean enforceSecurityChecks;
    
    public EventsResource() {
        enforceSecurityChecks = true;
    }
    
    /**
     * Gives test clients a way to test without the need to mock the complete Shiro environment
     */
    public EventsResource(boolean enforceSecurityChecks) {
        this.enforceSecurityChecks = enforceSecurityChecks;
    }
    
    @POST
    @Path("/createEvent")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/json;charset=UTF-8")
    public Response createEvent(
            @Context UriInfo uriInfo,
            @FormParam("eventName") String eventNameParam,
            @FormParam("eventdescription") String eventDescriptionParam,
            @FormParam("startdate") String startDateParam,
            @FormParam("startdateasmillis") Long startDateAsMillis,
            @FormParam("enddate") String endDateParam,
            @FormParam("enddateasmillis") Long endDateAsMillis,
            @FormParam("venuename") String venueNameParam, // takes precedence over lat/lng used for reverse geo-coding
            @FormParam("venuelat") String venueLat,
            @FormParam("venuelng") String venueLng,
            @FormParam("ispublic") String isPublicParam,
            @FormParam("officialwebsiteurl") String officialWebsiteURLParam,
            @FormParam("baseurl") String baseURLParam,
            @FormParam("leaderboardgroupids") List<String> leaderboardGroupIdsListParam,
            @FormParam("createleaderboardgroup") String createLeaderboardGroupParam,
            @FormParam("createregatta") String createRegattaParam,
            @FormParam("boatclassname") String boatClassNameParam,
            @FormParam("numberofraces") String numberOfRacesParam,
            @FormParam("canBoatsOfCompetitorsChangePerRace") boolean canBoatsOfCompetitorsChangePerRace,
            @FormParam("competitorRegistrationType") String competitorRegistrationType) throws ParseException, NotFoundException,
            NumberFormatException, IOException, org.json.simple.parser.ParseException, InvalidDateException {
        if (enforceSecurityChecks) {
            SecurityUtils.getSubject().checkPermission(Permission.EVENT.getStringPermission(Mode.CREATE));
        }
        final Response response;
        if (venueNameParam == null && (venueLat == null || venueLng == null)) {
            response = Response.status(Status.PRECONDITION_FAILED).entity("No venue specified; provide either venuename or venuelat/venuelng").build();
        } else {
            Triple<Event, LeaderboardGroup, RegattaLeaderboard> eventAndLeaderboardGroupAndLeaderboard = validateAndCreateEvent(uriInfo, eventNameParam, eventDescriptionParam, startDateParam,
                    startDateAsMillis, endDateParam, endDateAsMillis, venueNameParam,
                    /* venue latitude */ venueLat, /* venue longitude */ venueLng, isPublicParam, officialWebsiteURLParam,
                    baseURLParam, leaderboardGroupIdsListParam, createLeaderboardGroupParam, createRegattaParam,
                    boatClassNameParam, numberOfRacesParam, canBoatsOfCompetitorsChangePerRace, competitorRegistrationType);
            final JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("eventid", eventAndLeaderboardGroupAndLeaderboard.getA().getId().toString());
            jsonResponse.put("eventname", eventAndLeaderboardGroupAndLeaderboard.getA().getName());
            jsonResponse.put("eventstartdate", eventAndLeaderboardGroupAndLeaderboard.getA().getStartDate()==null?null:eventAndLeaderboardGroupAndLeaderboard.getA().getStartDate().asMillis());
            jsonResponse.put("eventenddate", eventAndLeaderboardGroupAndLeaderboard.getA().getEndDate()==null?null:eventAndLeaderboardGroupAndLeaderboard.getA().getEndDate().asMillis());
            if (eventAndLeaderboardGroupAndLeaderboard.getB() != null) {
                jsonResponse.put("leaderboardgroupid", eventAndLeaderboardGroupAndLeaderboard.getB().getId().toString());
            }
            if (eventAndLeaderboardGroupAndLeaderboard.getC() != null) {
                jsonResponse.put("regatta", eventAndLeaderboardGroupAndLeaderboard.getC().getRegatta().getName());
                jsonResponse.put("leaderboard", eventAndLeaderboardGroupAndLeaderboard.getC().getName());
            }
            response = ok(jsonResponse.toJSONString(), MediaType.APPLICATION_JSON);
        }
        return response;
    }
    
    @PUT
    @Path("/{eventId}/update")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/json;charset=UTF-8")
    public Response updateEvent(
            @Context UriInfo uriInfo,
            @PathParam("eventId") String eventId,
            @FormParam("eventName") String eventNameParam,
            @FormParam("eventdescription") String eventDescriptionParam,
            @FormParam("startdate") String startDateParam,
            @FormParam("startdateasmillis") Long startDateAsMillis,
            @FormParam("enddate") String endDateParam,
            @FormParam("enddateasmillis") Long endDateAsMillis,
            @FormParam("venuename") String venueNameParam, // takes precedence over lat/lng used for reverse geo-coding
            @FormParam("venuelat") String venueLat,
            @FormParam("venuelng") String venueLng,
            @FormParam("ispublic") String isPublicParam,
            @FormParam("officialwebsiteurl") String officialWebsiteURLParam,
            @FormParam("baseurl") String baseURLParam,
            @FormParam("leaderboardgroupids") List<String> leaderboardGroupIdsListParam,
            @FormParam("createleaderboardgroup") String createLeaderboardGroupParam,
            @FormParam("createregatta") String createRegattaParam,
            @FormParam("boatclassname") String boatClassNameParam,
            @FormParam("numberofraces") String numberOfRacesParam) throws ParseException, NotFoundException,
            NumberFormatException, IOException, org.json.simple.parser.ParseException, InvalidDateException {
        if (enforceSecurityChecks) {
            SecurityUtils.getSubject().checkPermission(Permission.EVENT.getStringPermission(Mode.CREATE));
        }
        final Response response;
        UUID id;
        try {
            id = toUUID(eventId);
        } catch (IllegalArgumentException e) {
            return getBadEventErrorResponse(eventId);
        }
        Event event = getService().getEvent(id);
        if (event == null) {
            response = getBadEventErrorResponse(eventId);
        } else {
            final String eventName, eventDescription, venueName;
            final TimePoint startDate, endDate;
            final URL officialWebsiteURL, baseURL;
            final boolean isPublic;
            final Iterable<UUID> leaderboardGroupIds;
            eventName = eventNameParam != null ? eventNameParam : event.getName();
            eventDescription = eventDescriptionParam != null ? eventDescriptionParam : event.getDescription();
            if (startDateParam != null || startDateAsMillis != null) {
                startDate = parseTimePoint(startDateParam, startDateAsMillis, null);
            } else {
                startDate = event.getStartDate();
            }
            if (endDateParam != null || endDateAsMillis != null) {
                endDate = parseTimePoint(endDateParam, endDateAsMillis, null);
            } else {
                endDate = event.getEndDate();
            }
            venueName = venueNameParam != null ? venueNameParam : (venueLat != null && venueLng != null) ? getDefaultVenueName(venueLat, venueLng) : event.getVenue().getName();
            isPublic = isPublicParam != null ? Boolean.valueOf(isPublicParam) : event.isPublic();
            if (leaderboardGroupIdsListParam.isEmpty()) {
                // nothing has been provided, not even an empty value; leave unchanged:
                leaderboardGroupIds = Util.map(event.getLeaderboardGroups(), lg->lg.getId());
            } else if (leaderboardGroupIdsListParam.size() == 1 && leaderboardGroupIdsListParam.get(0).isEmpty()) {
                // one empty occurrence of the sort "leaderboardgroupids=" means clear the value
                leaderboardGroupIds = Collections.emptyList();
            } else {
                leaderboardGroupIds = toUUIDList(leaderboardGroupIdsListParam);
            }
            officialWebsiteURL = officialWebsiteURLParam != null ? new URL(officialWebsiteURLParam) : event.getOfficialWebsiteURL();
            baseURL = baseURLParam != null ? new URL(baseURLParam) : event.getBaseURL();
            getService().updateEvent(id, eventName, eventDescription, startDate, endDate, venueName, isPublic,
                    leaderboardGroupIds, officialWebsiteURL, baseURL, event.getSailorsInfoWebsiteURLs(),
                    event.getImages(), event.getVideos(), event.getWindFinderReviewedSpotsCollectionIds());
            response = Response.ok().build();
        }
        return response;
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
    
    private RegattaLeaderboard validateAndCreateRegatta(String regattaNameParam, String boatClassNameParam, String scoringSchemeParam,
            UUID courseAreaId, String buoyZoneRadiusInHullLengthsParam, String useStartTimeInterferenceParam, String controlTrackingFromStartAndFinishTimesParam,
            String rankingMetricParam, List<Integer> leaderboardDiscardThresholdsParam, String numberOfRacesParam, boolean canBoatsOfCompetitorsChangePerRace,
            CompetitorRegistrationType competitorRegistrationType)
            throws ParseException, NotFoundException {
        boolean controlTrackingFromStartAndFinishTimes = controlTrackingFromStartAndFinishTimesParam == null ? false : Boolean.parseBoolean(controlTrackingFromStartAndFinishTimesParam);
        boolean useStartTimeInterference = useStartTimeInterferenceParam == null ? true : Boolean.parseBoolean(useStartTimeInterferenceParam);
        double buoyZoneRadiusInHullLengths = buoyZoneRadiusInHullLengthsParam == null ? 3.0 : Double.parseDouble(buoyZoneRadiusInHullLengthsParam);
        if (regattaNameParam == null) {
            throw new IllegalArgumentException(ExceptionManager.parameterRequiredMsg("regattaName"));
        }
        if (boatClassNameParam == null) {
            throw new IllegalArgumentException(ExceptionManager.parameterRequiredMsg("boatClassName"));
        }
        String regattaName = regattaNameParam;
        String boatClassName = boatClassNameParam;
        ScoringScheme scoringScheme = scoringSchemeParam == null ? createScoringScheme("LOW_POINT") : createScoringScheme(scoringSchemeParam);
        RankingMetrics rankingMetric = rankingMetricParam == null ? createRankingMetric("ONE_DESIGN") : createRankingMetric(rankingMetricParam);
        int[] leaderboardDiscardThresholds = leaderboardDiscardThresholdsParam == null ? new int[0] : leaderboardDiscardThresholdsParam.stream().mapToInt(i -> i).toArray();
        int numberOfRaces = numberOfRacesParam == null ? 0 : Integer.parseInt(numberOfRacesParam);
        RegattaCreationParametersDTO regattaCreationParametersDTO = new RegattaCreationParametersDTO(
                createDefaultSeriesCreationParameters(regattaName, numberOfRaces));
        UUID regattaId = UUID.randomUUID();
        addRegatta(regattaName, canBoatsOfCompetitorsChangePerRace, competitorRegistrationType, controlTrackingFromStartAndFinishTimes, useStartTimeInterference,
                buoyZoneRadiusInHullLengths, courseAreaId, boatClassName, /* startDate */ null, /* endDate */ null, scoringScheme,
                rankingMetric, regattaId, regattaCreationParametersDTO, leaderboardDiscardThresholds, numberOfRaces);
        final RegattaLeaderboard leaderboard = addLeaderboard(regattaName, leaderboardDiscardThresholds);
        SeriesCreationParametersDTO defaultSeries = regattaCreationParametersDTO.getSeriesCreationParameters().get("Default");
        addRaceColumns(regattaName, "Default", numberOfRaces);
        updateSeries(regattaName, defaultSeries);
        return leaderboard; 
    }

    /**
     * @param canBoatsOfCompetitorsChangePerRace 
     * @return the event created as first component; if a leaderboard group was to be created, the leaderboard group
     *         created as the second component or {@code null} otherwise; the regatta leaderboard as the third component
     *         in case a regatta was to be created, or {@code null} otherwise
     */
    private Util.Triple<Event, LeaderboardGroup, RegattaLeaderboard> validateAndCreateEvent(UriInfo uriInfo, String eventNameParam, String eventDescriptionParam,
            String startDateParam, Long startDateAsMillis, String endDateParam, Long endDateAsMillis,
            String venueNameParam, String venueLat, String venueLng, String isPublicParam,
            String officialWebsiteURLParam, String baseURLParam, List<String> leaderboardGroupIdsListParam,
            String createLeaderboardGroupParam, String createRegattaParam, String boatClassName,
            String numberOfRacesParam, boolean canBoatsOfCompetitorsChangePerRace,
            String competitorRegistrationTypeString) throws ParseException, NotFoundException, NumberFormatException, IOException,
            org.json.simple.parser.ParseException, InvalidDateException {
        boolean isPublic = isPublicParam == null ? false : Boolean.parseBoolean(isPublicParam);
        boolean createRegatta = createRegattaParam == null ? true : Boolean.parseBoolean(createRegattaParam);
        boolean createLeaderboardGroup = createLeaderboardGroupParam == null ? true : Boolean.parseBoolean(createLeaderboardGroupParam);
        String eventName = eventNameParam == null ? getDefaultEventName() : eventNameParam;
        String venueName = venueNameParam == null ? getDefaultVenueName(venueLat, venueLng) : venueNameParam;
        if (createRegatta && boatClassName == null) {
            throw new IllegalArgumentException(ExceptionManager.parameterRequiredMsg("boatClassName"));
        }
        String eventDescription = eventDescriptionParam == null ? eventName : eventDescriptionParam;
        final TimePoint startDate = parseTimePoint(startDateParam, startDateAsMillis, now());
        final TimePoint endDate = parseTimePoint(endDateParam, endDateAsMillis, new MillisecondsTimePoint(addOneWeek(startDate.asDate())));
        URL officialWebsiteURL = officialWebsiteURLParam == null ? null :  toURL(officialWebsiteURLParam);
        URL baseURL = baseURLParam == null ? uriInfo.getBaseUri().toURL() : toURL(baseURLParam);
        List<UUID> leaderboardGroupIds = leaderboardGroupIdsListParam == null ? new ArrayList<UUID>() : toUUIDList(leaderboardGroupIdsListParam);
        UUID eventId = UUID.randomUUID();
        // ignoring sailorsInfoWebsiteURLs, images, videos
        Map<Locale, URL> sailorsInfoWebsiteURLs = new HashMap<Locale,URL>();
        Iterable<ImageDescriptor> images = Collections.<ImageDescriptor> emptyList();
        Iterable<VideoDescriptor> videos = Collections.<VideoDescriptor> emptyList();
        Event event = createEvent(eventName, eventDescription, venueName, isPublic, startDate, endDate, officialWebsiteURL, baseURL,
                leaderboardGroupIds, eventId, sailorsInfoWebsiteURLs, images, videos);
        CourseArea courseArea = addCourseArea(event.getId(), "Default");
        final LeaderboardGroup leaderboardGroup;
        if (createLeaderboardGroup) {
            leaderboardGroup = validateAndAddLeaderboardGroup(event.getId(), event.getName(), event.getDescription(), /* leaderboardGroupDisplayNameParam */ null,
                    /* displayGroupsInReverseOrderParam */ false, /* leaderboardNamesParam */ null,
                    /* overallLeaderboardDiscardThresholdsParam */ null, /* overallLeaderboardScoringSchemeTypeParam */ null);
        } else {
            leaderboardGroup = null;
        }
        final CompetitorRegistrationType competitorRegistrationType;
        try {
            competitorRegistrationType = CompetitorRegistrationType.valueOf(competitorRegistrationTypeString);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(ExceptionManager.incorrectParameterValue(competitorRegistrationTypeString,
                    StringUtils.join(CompetitorRegistrationType.values(), ", ")));
        }
        final RegattaLeaderboard leaderboard;
        if (createRegatta) {
            leaderboard = validateAndCreateRegatta(event.getName(), boatClassName,
                    /* scoringSchemeParam */ null, courseArea.getId(), /* buoyZoneRadiusInHullLengthsParam */ null,
                    /* useStartTimeInterferenceParam */ null, /* controlTrackingFromStartAndFinishTimesParam */ null,
                    /* rankingMetricParam */ null, /* leaderboardDiscardThresholdsParam */ null,
                    numberOfRacesParam, canBoatsOfCompetitorsChangePerRace, competitorRegistrationType);
            if (leaderboardGroup != null) {
                getService().apply(new UpdateLeaderboardGroup(leaderboardGroup.getName(), leaderboardGroup.getName(),
                        leaderboardGroup.getDescription(), leaderboardGroup.getDisplayName(),
                        Collections.singletonList(leaderboard.getName()),
                        leaderboardGroup.getOverallLeaderboard() == null ? null
                                : ((ThresholdBasedResultDiscardingRule) leaderboardGroup.getOverallLeaderboard()
                                        .getResultDiscardingRule()).getDiscardIndexResultsStartingWithHowManyRaces(),
                        leaderboardGroup.getOverallLeaderboard() == null ? null
                                : leaderboardGroup.getOverallLeaderboard().getScoringScheme().getType()));
            }
        } else {
            leaderboard = null;
        }
        return new Util.Triple<>(event, leaderboardGroup, leaderboard);
    }
public static void main(String...a) {
    System.out.println(StringUtils.join(CompetitorRegistrationType.values(), ", "));
}
    private LeaderboardGroup validateAndAddLeaderboardGroup(UUID eventId, String leaderboardGroupName,
            String leaderboardGroupDescription, String leaderboardGroupDisplayName,
            boolean displayGroupsInReverseOrder, List<String> leaderboardNamesParam,
            List<Integer> overallLeaderboardDiscardThresholdsParam, String overallLeaderboardScoringSchemeTypeParam)
            throws NotFoundException {
        if (enforceSecurityChecks) {
            SecurityUtils.getSubject().checkPermission(Permission.LEADERBOARD_GROUP.getStringPermissionForObjects(Mode.CREATE, leaderboardGroupName));
        }
        ScoringSchemeType overallLeaderboardScoringSchemeType = overallLeaderboardScoringSchemeTypeParam == null
                ? null : getScoringSchemeType(overallLeaderboardScoringSchemeTypeParam);
        int[] overallLeaderboardDiscardThresholds = overallLeaderboardDiscardThresholdsParam == null ? new int[0] : overallLeaderboardDiscardThresholdsParam.stream().mapToInt(i -> i).toArray();
        List<String> leaderboardNames = leaderboardNamesParam == null ? new ArrayList<String>() : leaderboardNamesParam;
        LeaderboardGroup leaderboardGroup = getService().addLeaderboardGroup(UUID.randomUUID(), leaderboardGroupName,
                leaderboardGroupDescription, leaderboardGroupDisplayName, displayGroupsInReverseOrder, leaderboardNames,
                overallLeaderboardDiscardThresholds, overallLeaderboardScoringSchemeType);
        updateEvent(getEvent(eventId), leaderboardGroup);
        return leaderboardGroup;
    }

    private String getDefaultEventName() {
        final String username;
        if (enforceSecurityChecks) {
            username = getCurrentUser().getName();
        } else {
            username = "Dummy User";
        }
        return "Session "+username+" "+dateTimeFormat.format(new Date());
    }

    private User getCurrentUser() {
        User user = getSecurityService().getCurrentUser();
        return user;
    }

    private SecurityService getSecurityService() {
        BundleContext context = FrameworkUtil.getBundle(EventsResource.class).getBundleContext();
        return ServiceTrackerFactory.createAndOpen(context, SecurityService.class).getService();
    }
    
    private String getDefaultVenueName(String lat, String lng) throws NumberFormatException, IOException, org.json.simple.parser.ParseException {
        return ReverseGeocoder.INSTANCE.getPlacemarkNearest(new DegreePosition(Double.valueOf(lat), Double.valueOf(lng))).getName();
    }

    private void updateSeries(String regattaName, SeriesCreationParametersDTO defaultSeries) {
        if (enforceSecurityChecks) {
            SecurityUtils.getSubject().checkPermission(Permission.REGATTA.getStringPermissionForObjects(Mode.UPDATE, regattaName));
        }
        getService().apply(new UpdateSeries(new RegattaName(regattaName), "Default", "Default", defaultSeries.isMedal(),
                defaultSeries.isFleetsCanRunInParallel(), defaultSeries.getDiscardingThresholds(),
                defaultSeries.isStartsWithZero(), defaultSeries.isFirstColumnIsNonDiscardableCarryForward(),
                defaultSeries.hasSplitFleetContiguousScoring(), defaultSeries.getMaximumNumberOfDiscards(),
                defaultSeries.getFleets()));
    }
    
    private void addRaceColumns(String regattaName, String seriesName, int numberOfRaces) {
        final Regatta regatta = findRegattaByName(regattaName);
        if (regatta == null) {
            throw new IllegalArgumentException(ExceptionManager.objectNotFoundMsg("regatta", regattaName));
        }
        int oneBasedNumberOfNextRace = Util.size(regatta.getRaceColumns())+1;
        for (int i = 1; i <= numberOfRaces; i++) {
            addRaceColumn(regattaName, seriesName, "R"+oneBasedNumberOfNextRace++);
        }
    }

    private RaceColumnInSeries addRaceColumn(String regattaName, String seriesName, String columnName) {
        if (enforceSecurityChecks) {
            SecurityUtils.getSubject().checkPermission(Permission.REGATTA.getStringPermissionForObjects(Mode.UPDATE, regattaName));
        }
        return getService().apply(new AddColumnToSeries(new RegattaName(regattaName), seriesName, columnName));
    }

    private Event createEvent(String eventName, String eventDescription, String venueName, boolean isPublic,
            TimePoint startDate, TimePoint endDate, URL officialWebsiteURL, URL baseURL,
            List<UUID> leaderboardGroupIds, UUID eventId, Map<Locale, URL> sailorsInfoWebsiteURLs,
            Iterable<ImageDescriptor> images, Iterable<VideoDescriptor> videos) {
        if (enforceSecurityChecks) {
            SecurityUtils.getSubject().checkPermission(Permission.EVENT.getStringPermissionForObjects(Mode.CREATE, eventName));
        }
        return getService()
                .apply(new CreateEvent(eventName, eventDescription, startDate, endDate, venueName, isPublic, eventId,
                        officialWebsiteURL, baseURL, sailorsInfoWebsiteURLs, images, videos, leaderboardGroupIds));
    }
    
    private void updateEvent(Event event, LeaderboardGroup leaderboardGroup){
        if (enforceSecurityChecks) {
            SecurityUtils.getSubject().checkPermission(Permission.EVENT.getStringPermissionForObjects(Mode.UPDATE, event.getId().toString()));
        }
        List<UUID> newLeaderboardGroupIds = new ArrayList<>();
        StreamSupport.stream(event.getLeaderboardGroups().spliterator(), false)
                .forEach(lg -> newLeaderboardGroupIds.add(lg.getId()));
        newLeaderboardGroupIds.add(leaderboardGroup.getId());
        getService().updateEvent(event.getId(), event.getName(), event.getDescription(), event.getStartDate(),
                event.getEndDate(), event.getVenue().getName(), event.isPublic(), newLeaderboardGroupIds,
                event.getOfficialWebsiteURL(), event.getBaseURL(), event.getSailorsInfoWebsiteURLs(), event.getImages(),
                event.getVideos(), event.getWindFinderReviewedSpotsCollectionIds());
    }

    private CourseArea addCourseArea(UUID eventId, String courseAreaName) {
        if (enforceSecurityChecks) {
            SecurityUtils.getSubject().checkPermission(Permission.EVENT.getStringPermissionForObjects(Mode.UPDATE, eventId.toString()));
        }
        String[] courseAreaNames = new String[] { courseAreaName };
        UUID[] courseAreaIds = new UUID[] { UUID.randomUUID() };
        return getService().apply(new AddCourseAreas(eventId, courseAreaNames, courseAreaIds))[0];
    }

    private Regatta addRegatta(String regattaName, boolean canBoatsOfCompetitorsChangePerRace, 
            CompetitorRegistrationType competitorRegistrationType, boolean controlTrackingFromStartAndFinishTimes, boolean useStartTimeInterference,
            double buoyZoneRadiusInHullLengths, UUID courseAreaId, String boatClassName,
            MillisecondsTimePoint startDate, MillisecondsTimePoint endDate, ScoringScheme scoringScheme,
            RankingMetrics rankingMetric, UUID regattaId, RegattaCreationParametersDTO regattaCreationParametersDTO,
            int[] leaderboardDiscardThresholds, int numberOfRaces) {
        if (enforceSecurityChecks) {
            SecurityUtils.getSubject().checkPermission(Permission.REGATTA.getStringPermissionForObjects(Mode.CREATE, regattaName));
        }
        Regatta regatta = getService().apply(new AddSpecificRegatta(regattaName, boatClassName, canBoatsOfCompetitorsChangePerRace,
                competitorRegistrationType, startDate, endDate, regattaId,
                regattaCreationParametersDTO, /* isPersistent */ true, scoringScheme, courseAreaId, buoyZoneRadiusInHullLengths,
                useStartTimeInterference, controlTrackingFromStartAndFinishTimes, rankingMetric));
        return regatta;
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
            
            ResultDiscardingRule rule = defaultLeaderboardGroup.getOverallLeaderboard()==null?null:defaultLeaderboardGroup.getOverallLeaderboard().getResultDiscardingRule();
            int[] overallLeaderboardDiscardThresholds = null; 
            if(rule instanceof ThresholdBasedResultDiscardingRule){
                ThresholdBasedResultDiscardingRule resultDiscardingRule = (ThresholdBasedResultDiscardingRule) rule;
                overallLeaderboardDiscardThresholds = resultDiscardingRule.getDiscardIndexResultsStartingWithHowManyRaces();
            }
            
            getService().updateLeaderboardGroup(defaultLeaderboardGroup.getName(), defaultLeaderboardGroup.getName(), defaultLeaderboardGroup.getDescription(),
                    defaultLeaderboardGroup.getDisplayName(), leaderboards, overallLeaderboardDiscardThresholds, 
                    defaultLeaderboardGroup.getOverallLeaderboard()==null?null:defaultLeaderboardGroup.getOverallLeaderboard().getScoringScheme().getType());
        }
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

    private RegattaLeaderboard createRegattaLeaderboard(String regattaName, int[] discardThresholds) {
        if (enforceSecurityChecks) {
            SecurityUtils.getSubject().checkPermission(Permission.LEADERBOARD.getStringPermissionForObjects(Mode.CREATE, regattaName));
        }
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
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
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
        try {
            return RankingMetrics.valueOf(rankingMetricParam);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(ExceptionManager.incorrectParameterValue(rankingMetricParam,
                    getEnumValuesAsString(RankingMetrics.class)));
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
        try {
            return ScoringSchemeType.valueOf(scoringSchemeTypeParam);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(ExceptionManager.incorrectParameterValue(scoringSchemeTypeParam,
                    getEnumValuesAsString(ScoringSchemeType.class)));
        }
    }

    private Event getEvent(UUID eventId) throws NotFoundException {
        Event event = getService().getEvent(eventId);
        if (event != null) {
            return event;
        }
        throw new NotFoundException(ExceptionManager.objectNotFoundMsg(Event.class.getSimpleName(), eventId));
    }

    private <T> Stream<T> stream(Spliterator<T> spliterator) {
        return StreamSupport.stream(spliterator, false);
    }
}
