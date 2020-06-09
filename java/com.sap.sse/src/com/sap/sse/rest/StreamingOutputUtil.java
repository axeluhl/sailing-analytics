package com.sap.sse.rest;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.ws.rs.core.StreamingOutput;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class StreamingOutputUtil {
    protected StreamingOutput streamingOutput(JSONObject jsonObject) {
        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException {
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                        output, "UTF8"));
                jsonObject.writeJSONString(bufferedWriter);
                bufferedWriter.flush();
            }
        };
    }

    protected StreamingOutput streamingOutput(JSONArray jsonArray) {
        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException {
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                        output, "UTF8"));
                jsonArray.writeJSONString(bufferedWriter);
                bufferedWriter.flush();
            }
        };
    }
    
    /**
     * If the {@code entity} is a {@link StreamingOutput}, the stream will first be read and then converted to a
     * {@link String}. Otherwise, the {@link Object#toString()} method is used to convert the {@code entity} into a
     * string.
     */
    public static String getEntityAsString(Object entity) throws IOException {
        final String result;
        if (entity instanceof StreamingOutput) {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ((StreamingOutput) entity).write(bos);
            return bos.toString("UTF8");
        } else {
            result = entity.toString();
        }
        return result;
    }
}
