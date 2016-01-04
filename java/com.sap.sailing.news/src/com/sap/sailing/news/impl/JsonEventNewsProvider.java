package com.sap.sailing.news.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
    
    private static URL staticNewsURL;
    
    private List<InfoEventNewsItem> news = new LinkedList<>();
    private long nextUpdate = 0;
    
    static {
        try {
            staticNewsURL = new URL("http://static.sapsailing.com/events_news/news.json");
        } catch (MalformedURLException e) {
        }
    }
    
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
                logger.fine("Updating news from URL "+staticNewsURL);
                URLConnection urlConnection = staticNewsURL.openConnection();
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
                    String title = (String) eventAsJson.get("title");
                    Map<Locale, String> titles = readI18nText(eventAsJson, "titles");
                    String message = (String) eventAsJson.get("message");
                    Map<Locale, String> messages = readI18nText(eventAsJson, "messages");
                    String newsURL = (String) eventAsJson.get("url");
                    UUID eventId = UUID.fromString(eventIdAsString);
                    InfoEventNewsItem newsItem = new InfoEventNewsItem(eventId, title, message, new Date(timestamp.longValue()), null, newsURL == null ? null : new URL(newsURL), titles, messages);
                    newNews.add(newsItem);
                }
                news = newNews;
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        } catch (IOException | ParseException e) {
            logger.log(Level.INFO, "Exception trying to fetch news from " + staticNewsURL, e);
        }
    }
    
    private Map<Locale, String> readI18nText(JSONObject eventAsJson, String field) {
        Object object = eventAsJson.get(field);
        if(object == null) {
            return Collections.emptyMap();
        }
        JSONObject texts = (JSONObject) object;
        Set<Entry<Object, Object>> entrySet = texts.entrySet();
        Map<Locale, String> result = new HashMap<>();
        for (Entry<Object, Object> entry : entrySet) {
            result.put(Locale.forLanguageTag((String) entry.getKey()), (String) entry.getValue());
        }
        return result;
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
