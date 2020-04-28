package com.sap.sse.rest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.ws.rs.core.StreamingOutput;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public abstract class StreamingOutputUtil {
    protected StreamingOutput streamingOutput(JSONObject jsonObject) {
        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException {
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(output));
                jsonObject.writeJSONString(bufferedWriter);
                bufferedWriter.flush();
            }
        };
    }

    protected StreamingOutput streamingOutput(JSONArray jsonArray) {
        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException {
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(output));
                jsonArray.writeJSONString(bufferedWriter);
                bufferedWriter.flush();
            }
        };
    }
}
