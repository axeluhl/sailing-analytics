package com.sap.sailing.news.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.news.EventNewsItem;
import com.sap.sailing.news.EventNewsProvider;

public class JsonEventNewsProvider implements EventNewsProvider {
    private static final int UPDATE_INTERVAL = 60_000;
    private static final int CONNECTION_TIMEOUT = 15_000;

    private static final Logger logger = Logger.getLogger(JsonEventNewsProvider.class.getName());
    
    // TODO fmittag: use correct URL
    private static final URL url = JsonEventNewsProvider.class.getResource("news.json");
    
    private List<InfoEventNewsItem> news = new LinkedList<>();
    private long nextUpdate = 0;
    
    public JsonEventNewsProvider() {
    }
    
    private synchronized void update() {
        if(nextUpdate >= System.currentTimeMillis()) {
            // update already done
            return;
        }
        nextUpdate = System.currentTimeMillis() + UPDATE_INTERVAL;
        
        try {
            BufferedReader bufferedReader = null;
            try {
                logger.fine("Updating news from URL "+url);
                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
                urlConnection.connect();
                bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                JSONParser parser = new JSONParser();
                Object newsAsObject = parser.parse(bufferedReader);
                JSONArray newsAsJsonArray = (JSONArray) newsAsObject;
                List<InfoEventNewsItem> newNews = new LinkedList<>();
                for (Object eventAsObject : newsAsJsonArray) {
                    JSONObject eventAsJson = (JSONObject) eventAsObject;
                    String eventIdAsString = (String) eventAsJson.get("event");
                    Number timestamp = (Number) eventAsJson.get("timestamp");
                    String title = (String) eventAsJson.get("tite");
                    String message = (String) eventAsJson.get("message");
                    String newsURL = (String) eventAsJson.get("url");
                    UUID eventId = UUID.fromString(eventIdAsString);
                    InfoEventNewsItem newsItem = new InfoEventNewsItem(eventId, title, message, new Date(timestamp.longValue()), null, newsURL == null ? null : new URL(newsURL));
                    newNews.add(newsItem);
                }
                news = newNews;
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        } catch (IOException | ParseException e) {
            logger.log(Level.INFO, "Exception trying to fetch news from " + url, e);
        }
    }
    
    @Override
    public Collection<? extends EventNewsItem> getNews(Event event) {
        if(nextUpdate < System.currentTimeMillis()) {
            update();
        }
        
        UUID eventId = event.getId();
        List<InfoEventNewsItem> result = new LinkedList<>();
        for(InfoEventNewsItem newsEntry: news) {
            if(eventId.equals(newsEntry.getEventUUID())) {
                result.add(newsEntry);
            }
        }
        
        return result;
    }

    @Override
    public Collection<? extends EventNewsItem> getNews(Event event, Date startingFrom) {
        return getNews(event);
    }

    @Override
    public boolean hasNews(Event event, Date startingFrom) {
        return news.size() > 0;
    }

    @Override
    public boolean hasNews(Event event) {
        return news.size() > 0;
    }

}
