package com.sap.sailing.gwt.ui.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
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
import com.sap.sailing.domain.common.NotFoundException;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.debranding.ClientConfigurationServlet;
import com.sap.sse.security.SecurityService;
import com.sap.sse.shared.media.ImageDescriptor;

public class SharingServlet extends HttpServlet {
    private static final long serialVersionUID = 6990478954607011261L;
    private static final Logger logger = Logger.getLogger(ClientConfigurationServlet.class.getName());

    private static final String GWT_HOME_HTML = "/gwt/Home.html";
    private static final String SHARED_PROXY_HTML = "/SharedProxy.html";
    private static final String EVENTS = "events";
    private static final Object SERIES = "series";
    private static final String PATH_SEPARATOR = "/";
    private static final String DEFAULT_TITLE = "SAP Sailing";
    private static final String DEFAULT_DESCRIPTION = "Help sailors analyze performance and optimize strategy &#8226; Bring fans closer to the action " +
            "&#8226; Provide the media with information and insights to deliver a greater informed commentary";
    private static final String DEFAULT_IMAGE_URL = "https://www.sapsailing.com/gwt/com.sap.sailing.gwt.home.Home/5A1CE55C422F0249466090CC8E55CC96.cache.jpg";
    
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
                ServletContext servletContext = this.getServletContext();
                try (InputStream in = servletContext.getResourceAsStream(SHARED_PROXY_HTML)) {
                    String content = readInputStreamToString(in);
                    final ServletContext reqServletContext = req.getServletContext();
                    String resourceAdress = "http://" + req.getLocalAddr() + ":" + req.getLocalPort() + GWT_HOME_HTML;
                    final Map<String, String> createReplacementMap = createReplacementMap(pathInfo, reqServletContext,
                            resourceAdress);
                    for (Map.Entry<String, String> item : createReplacementMap.entrySet()) {
                        content = content.replace("${" + item.getKey() + "}", item.getValue());
                    }
                    final byte[] bytes = content.getBytes();
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
            String resourceAdress) throws Exception {
        final SecurityService securityService = getSecurityService(servletContext);
        final RacingEventService eventService = getEventService(servletContext);
        final Map<String, String> map = new HashMap<>();
        final String[] split = pathInfo.replaceFirst(PATH_SEPARATOR, "").split(PATH_SEPARATOR);
        int splitLength = split.length;
        String placeUrl = resourceAdress;
        String title = DEFAULT_TITLE;
        String description = DEFAULT_DESCRIPTION;
        String imageUrl = DEFAULT_IMAGE_URL;
        // The first path variable in the pattern is the type of resource. Either series or event
        if (splitLength > 1 && EVENTS.equals(split[0])) {
            if(splitLength == 4 && split[2] != null) {
                placeUrl += "#/regatta/overview";
            }else {
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
                for (ImageDescriptor imageDescription : event.getImages()) {
                    imageUrl = imageDescription.getURL().toString();
                    break;
                }
                // The item at third position will always be regattas if present.
                // TODO: change to UUID, when regattaIdentifiers are changed
                if(splitLength == 4 && split[2] != null) {
                    final String regattaIdentifier = split[3];
                    Regatta regatta = eventService.getRegattaByName(regattaIdentifier);
                    if (regatta != null) {
                        placeUrl += "&regattaId=" + regattaIdentifier;
//                                + URLEncoder.encode(regattaIdentifier, StandardCharsets.UTF_8.toString());
                    }else {
                        throw new NotFoundException("No regatta with identifier:" + regattaIdentifier);
                    }
                }
            } else {
                throw new NotFoundException("No event with id:" + eventId);
            }
        } else if(splitLength == 2 && SERIES.equals(split[0])){
            placeUrl += "#/series";
            final UUID leaderboardGroupId = UUID.fromString(split[1]);
            final LeaderboardGroup leaderboardGroup = eventService.getLeaderboardGroupByID(leaderboardGroupId);
            if(leaderboardGroup != null) {
                securityService.checkCurrentUserReadPermission(leaderboardGroup);
                placeUrl += "/:leaderboardGroupId=" + leaderboardGroupId;
                title = leaderboardGroup.getName();
                final String lbgDescription = leaderboardGroup.getDescription();
                if (lbgDescription != null && !lbgDescription.equals("")) {
                    description = lbgDescription;
                }
                //TODO: find image for series.
            }
        }else {
            throw new IllegalArgumentException("path did not contain appropriate amount of arguments");
        }
        map.put("title", title);
        map.put("description", description);
        map.put("display_url", placeUrl);
        map.put("redirect_url", placeUrl);
        map.put("image", imageUrl);
        return map;
    }
}