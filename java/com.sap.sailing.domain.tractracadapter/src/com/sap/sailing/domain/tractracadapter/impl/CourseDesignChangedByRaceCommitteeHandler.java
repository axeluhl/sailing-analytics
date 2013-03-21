package com.sap.sailing.domain.tractracadapter.impl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.CourseData;
import com.sap.sailing.domain.tracking.CourseDesignChangedListener;
import com.sap.sailing.domain.tractracadapter.CourseUpdateResponse;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.ControlPointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseDataJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.GateJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.WaypointJsonSerializer;

public class CourseDesignChangedByRaceCommitteeHandler implements CourseDesignChangedListener {
    
    private JsonSerializer<CourseData> courseSerializer;
    private JsonDeserializer<CourseUpdateResponse> courseUpdateDeserializer;
    private final URI courseDesignUpdateURI;
    private final Serializable regattaId;
    private final Serializable raceId;
    
    public CourseDesignChangedByRaceCommitteeHandler(URI courseDesignUpdateURI, Serializable regattaId, Serializable raceId) {
        this.courseDesignUpdateURI = courseDesignUpdateURI;
        this.regattaId = regattaId;
        this.raceId = raceId;
        this.courseSerializer = new CourseJsonSerializer(
                new CourseDataJsonSerializer(
                        new WaypointJsonSerializer(
                                new ControlPointJsonSerializer(
                                        new MarkJsonSerializer(), 
                                        new GateJsonSerializer(new MarkJsonSerializer())))));
        this.courseUpdateDeserializer = new CourseUpdateResponseDeserializer();
    }

    @Override
    public void courseDesignChanged(CourseData newCourseDesign) throws MalformedURLException, IOException {
        JSONObject serializedCourseDesign = courseSerializer.serialize(newCourseDesign);
        String payload = serializedCourseDesign.toJSONString();

        URL currentCourseDesignURL = buildCourseUpdateURL();
        HttpURLConnection connection = (HttpURLConnection) currentCourseDesignURL.openConnection();
        try {
            setConnectionProperties(connection, payload);

            sendWithPayload(connection, payload);

            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            try {
                Object responseBody = JSONValue.parseWithException(reader);
                JSONObject responseObject = Helpers.toJSONObjectSafe(responseBody);

                CourseUpdateResponse courseUpdateResponse = courseUpdateDeserializer.deserialize(responseObject);
                if (courseUpdateResponse.getStatus().equals("FAILURE")) {
                    System.out.println(courseUpdateResponse.getMessage());
                }
            } catch (ParseException pe) {
                pe.printStackTrace();
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void sendWithPayload(HttpURLConnection connection, String payload) throws IOException {
        DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
        writer.writeBytes(payload);
        writer.flush();
        writer.close();
    }

    private void setConnectionProperties(HttpURLConnection connection, String payload) throws ProtocolException {
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.addRequestProperty("Content-Length", String.valueOf(payload.getBytes().length));
    }
    
    private URL buildCourseUpdateURL() throws MalformedURLException, UnsupportedEncodingException {
        String url = String.format("%s?eventid=%s&raceid=%s", 
                this.courseDesignUpdateURI.toString(), 
                URLEncoder.encode(this.regattaId.toString(), "UTF-8"), 
                URLEncoder.encode(this.raceId.toString(), "UTF-8"));
        return new URL(url);
    }

}
