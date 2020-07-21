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
import com.sap.sailing.domain.common.NotFoundException;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.debranding.ClientConfigurationServlet;
import com.sap.sse.security.SecurityService;
import com.sap.sse.shared.media.ImageDescriptor;

public class SharingServlet extends HttpServlet {
    private static final String GWT_HOME_HTML = "/gwt/Home.html";
    private static final String SHARED_PROXY_HTML_RELATIVE_PATH = "/SharedProxy.html";
    private static final String EVENTS = "events";
    private static final long serialVersionUID = 6990478954607011261L;
    private static final Logger logger = Logger.getLogger(ClientConfigurationServlet.class.getName());

    private static final String PATH_SEPARATOR = "/";
    private static final String DEFAULT_TITLE = "SAP Sailing";
    private static final String DEFAULT_DESCRIPTION = "Sailing provides the perfect platform for SAP to showcase solutions and help the sport run like never before.";
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
        final String servletPath = req.getServletPath();
        final String pathInfo = req.getPathInfo();
        if (pathInfo != null) {
            final byte[] cachedPage;
            resp.setContentType(MediaType.TEXT_HTML);
            if ((cachedPage = cache.get(servletPath)) != null) {
                resp.getOutputStream().write(cachedPage);
            } else {
                try (InputStream in = this.getServletContext().getResourceAsStream(SHARED_PROXY_HTML_RELATIVE_PATH)) {
                    String content = readInputStreamToString(in);
                    final ServletContext servletContext = req.getServletContext();

                    String resourceAdress = "http://" + req.getLocalAddr() + ":" + req.getLocalPort() + GWT_HOME_HTML;
                    final Map<String, String> createReplacementMap = createReplacementMap(pathInfo, servletContext,
                            resourceAdress);
                    for (Map.Entry<String, String> item : createReplacementMap.entrySet()) {
                        content = content.replace("${" + item.getKey() + "}", item.getValue());
                    }
                    final byte[] bytes = content.getBytes();
                    resp.getOutputStream().write(bytes);
                    cache.computeIfAbsent(servletPath, key -> bytes);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "could not process or read resource " + servletPath, e);
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            logger.log(Level.WARNING, "no resource specified " + servletPath);
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
        String placeUrl = resourceAdress;
        String title = DEFAULT_TITLE;
        String description = DEFAULT_DESCRIPTION;
        String imageUrl = DEFAULT_IMAGE_URL;
        // The first path variable in the pattern is the type of resource. Either series or event
        if (split.length > 1 && EVENTS.equals(split[0])) {
            placeUrl += "#/event";
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
            } else {
                throw new NotFoundException("No event with id:" + eventId);
            }
        } else {
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