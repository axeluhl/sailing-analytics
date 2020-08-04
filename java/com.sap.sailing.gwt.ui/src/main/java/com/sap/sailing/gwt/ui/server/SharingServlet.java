package com.sap.sailing.gwt.ui.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.security.SecurityService;

public class SharingServlet extends HttpServlet {
    private static final String DEFAULT_TEASER_URL = "http://media.sapsailing.com/2014/505Worlds/Images_Homepage/505Worlds2014_eventteaser.jpg";
    private static final String SHARED_PROXY_RESOURCE = "/com/sap/sailing/gwt/ui/opengraph/SharedProxy.html";
    private static final long serialVersionUID = 6990478954607011261L;
    private static final Logger logger = Logger.getLogger(SharingServlet.class.getName());

    private static final String GWT_PREFIX = "/gwt";
    private static final String HOME_HTML = "/Home.html";
    private static final String EVENTS = "events";
    private static final Object SERIES = "series";
    private static final String PATH_SEPARATOR = "/";
    private static final String DEFAULT_TITLE = "SAP Sailing";
    private static final String DEFAULT_DESCRIPTION = "Help sailors analyze performance and optimize strategy &#8226; Bring fans closer to the action "
            + "&#8226; Provide the media with information and insights to deliver a greater informed commentary";

    private final ConcurrentHashMap<String, byte[]> cache = new ConcurrentHashMap<>();

    protected <T> T getService(Class<T> clazz) {
        BundleContext context = Activator.getDefault();
        ServiceTracker<T, T> tracker = new ServiceTracker<T, T>(context, clazz, null);
        tracker.open();
        T service = tracker.getService();
        tracker.close();
        return service;
    }

    private SecurityService getSecurityService(ServletContext servletContext) {
        return getService(SecurityService.class);
    }

    private RacingEventService getEventService(ServletContext servletContext) {
        return getService(RacingEventService.class);
    }

    /**
     * All shared URLs follow one of these patterns: HOST/shared/event/{eventId}/
     * HOST/shared/event/{eventId}/regatta/{regattaId} HOST/gwt/shared/series/{leaderboardGroupId}/
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String pathInfo = req.getPathInfo();
        if (pathInfo != null) {
            final byte[] cachedPage;
            resp.setContentType(MediaType.TEXT_HTML);
            if ((cachedPage = cache.get(pathInfo)) != null) {
                resp.getOutputStream().write(cachedPage);
            } else {
                ClassLoader classLoader = getClass().getClassLoader();
                try (InputStream in = classLoader.getResourceAsStream(SHARED_PROXY_RESOURCE)) {
                    String content = readInputStreamToString(in);
                    final ServletContext reqServletContext = req.getServletContext();
                    final Map<String, String> createReplacementMap = createReplacementMap(pathInfo, reqServletContext,
                            req, classLoader);
                    for (Map.Entry<String, String> item : createReplacementMap.entrySet()) {
                        content = content.replace("${" + item.getKey() + "}", item.getValue());
                    }
                    final byte[] bytes = content.getBytes(Charset.forName("UTF-8"));
                    resp.getOutputStream().write(bytes);
                    cache.computeIfAbsent(pathInfo, key -> bytes);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "could not process or read resource " + pathInfo, e);
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            logger.log(Level.WARNING, "no resource specified " + pathInfo);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private String readInputStreamToString(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        in.close();
        buffer.flush();
        String content = buffer.toString();
        buffer.close();
        return content;
    }

    private Map<String, String> createReplacementMap(String pathInfo, ServletContext servletContext,
            HttpServletRequest req, ClassLoader classLoader) throws Exception {
        final SecurityService securityService = getSecurityService(servletContext);
        final RacingEventService eventService = getEventService(servletContext);
        final Map<String, String> map = new HashMap<>();
        final String[] split = pathInfo.replaceFirst(PATH_SEPARATOR, "").split(PATH_SEPARATOR);
        final int splitLength = split.length;
        final String serverAddress = "http://" + req.getServerName() + ":" + req.getServerPort();
        String placeUrl = serverAddress + GWT_PREFIX + HOME_HTML;
        String title = DEFAULT_TITLE;
        String description = DEFAULT_DESCRIPTION;
        String imageUrl = DEFAULT_TEASER_URL;
        // The first path variable in the pattern is the type of resource. Either series or event
        if (splitLength > 1 && EVENTS.equals(split[0])) {
            if (splitLength == 4 && split[2] != null) {
                placeUrl += "#/regatta/overview";
            } else {
                placeUrl += "#/event";
            }
            final UUID eventId = UUID.fromString(split[1]);
            final Event event = eventService.getEvent(eventId);
            if (event != null) {
                securityService.checkCurrentUserReadPermission(event);
                placeUrl += "/:eventId=" + eventId;
                title = event.getName();
                final String eventDescription = event.getDescription();
                if (eventDescription != null && !eventDescription.equals("")) {
                    description = eventDescription;
                }
                final String specificTeaserImageUrl = findSpecificTeaserImageUrl(event);
                if (specificTeaserImageUrl != null) {
                    imageUrl = specificTeaserImageUrl;
                }
                // The item at third position will always be regattas if present.
                // TODO: change to UUID, when regattaIdentifiers are changed
                if (splitLength == 4 && split[2] != null) {
                    final String regattaIdentifier = split[3];
                    Leaderboard leaderboardByName = eventService.getLeaderboardByName(regattaIdentifier);
                    if (leaderboardByName != null) {
                        placeUrl += "&regattaId=" + regattaIdentifier;
                        title = leaderboardByName.getDisplayName();
                    } else {
                        Regatta regattaByName = eventService.getRegattaByName(regattaIdentifier);
                        if (regattaByName != null) {
                            placeUrl += "&regattaId=" + regattaIdentifier;
                            title = regattaByName.getName();
                        } else {
                            placeUrl = serverAddress;
                        }
                    }
                }
            } else {
                placeUrl = serverAddress;
            }
        } else if (splitLength == 2 && SERIES.equals(split[0])) {
            placeUrl += "#/series";
            final UUID leaderboardGroupId = UUID.fromString(split[1]);
            final LeaderboardGroup leaderboardGroup = eventService.getLeaderboardGroupByID(leaderboardGroupId);
            if (leaderboardGroup != null) {
                securityService.checkCurrentUserReadPermission(leaderboardGroup);
                placeUrl += "/:leaderboardGroupId=" + leaderboardGroupId;
                title = leaderboardGroup.getName();
                final String lbgDescription = leaderboardGroup.getDescription();
                if (lbgDescription != null && !lbgDescription.equals("")) {
                    description = lbgDescription;
                }
                final List<Event> eventsForSeriesOrdered = HomeServiceUtil.getEventsForSeriesOrdered(leaderboardGroup, eventService);
                if(!eventsForSeriesOrdered.isEmpty()) {
                    final Event event = eventsForSeriesOrdered.get(eventsForSeriesOrdered.size() -1);
                    final String specificTeaserImageUrl = findSpecificTeaserImageUrl(event);
                    if(specificTeaserImageUrl != null) {
                        imageUrl = specificTeaserImageUrl;
                    }
                }
            }else {
                placeUrl = serverAddress;
            }
        } else {
            placeUrl = serverAddress;
        }
        map.put("title", title);
        map.put("description", description);
        map.put("display_url", placeUrl);
        map.put("redirect_url", placeUrl);
        map.put("image", imageUrl);
        return map;
    }

    private String findSpecificTeaserImageUrl(final Event event) {
        final String thumbnailUrl = HomeServiceUtil.findEventThumbnailImageUrlAsString(event);
        if(thumbnailUrl != null) {
            return thumbnailUrl;
        }else {
            final String stageImageUrl = HomeServiceUtil.getStageImageURLAsString(event);
            if(stageImageUrl != null) {
                return stageImageUrl;
            }
        }
        return null;
    }
}