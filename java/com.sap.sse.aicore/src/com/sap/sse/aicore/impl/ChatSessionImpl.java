package com.sap.sse.aicore.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.sap.sse.aicore.AICore;
import com.sap.sse.aicore.ChatSession;
import com.sap.sse.aicore.Credentials;
import com.sap.sse.aicore.Deployment;
import com.sap.sse.common.Util;

public class ChatSessionImpl implements ChatSession {
    private final static String CHAT_PATH_TEMPLATE = "/v2/inference/deployments/%s/chat/completions?api-version=2024-06-01";
    private final List<String> systemPrompts;
    private final List<String> userPrompts;
    private final AICore aiCore;
    private Deployment deployment;
    
    public ChatSessionImpl(final AICore aiCore, final Deployment deployment) {
        this.aiCore = aiCore;
        this.deployment = deployment;
        this.systemPrompts = new ArrayList<>();
        this.userPrompts = new ArrayList<>();
    }

    public ChatSessionImpl(final Credentials credentials, final String modelName) throws UnsupportedOperationException,
            ClientProtocolException, URISyntaxException, IOException, ParseException {
        this(AICore.create(credentials), modelName);
    }
    
    public ChatSessionImpl(final AICore aiCore, final String modelName) throws UnsupportedOperationException, ClientProtocolException, URISyntaxException, IOException, ParseException {
        this(aiCore, aiCore.getDeploymentByModelName(modelName).get());
    }
    
    @Override
    public ChatSession addSystemPrompt(String prompt) {
        systemPrompts.add(prompt);
        return this;
    }

    @Override
    public ChatSession addPrompt(String prompt) {
        userPrompts.add(prompt);
        return this;
    }
    
    private String getChatPath() {
        return String.format(CHAT_PATH_TEMPLATE, deployment.getId());
    }

    private void addPromptMessages(final Iterable<String> prompts, final String role, JSONArray messagesToAddTo) {
        for (final String prompt : prompts) {
            final JSONObject message = new JSONObject();
            message.put("role", role);
            message.put("content", prompt);
            messagesToAddTo.add(message);
        }
    }
    
    @Override
    public String submit() throws UnsupportedOperationException, ClientProtocolException, URISyntaxException, IOException, ParseException {
        final HttpPost postRequest = aiCore.getHttpPostRequest(getChatPath());
        final JSONObject toSubmit = new JSONObject();
        final JSONArray messages = new JSONArray();
        toSubmit.put("messages", messages);
        addPromptMessages(systemPrompts, "system", messages);
        addPromptMessages(userPrompts, "user", messages);
        postRequest.setEntity(new StringEntity(toSubmit.toJSONString(), ContentType.APPLICATION_JSON));
        final JSONObject chatResponse = aiCore.getJSONResponse(postRequest);
        final List<String> results = new ArrayList<>();
        for (final Object choice : ((JSONArray) chatResponse.get("choices"))) {
            final JSONObject choiseJson = (JSONObject) choice;
            final JSONObject message = (JSONObject) choiseJson.get("message");
            final String content = message.get("content").toString();
            results.add(content);
        }
        return Util.joinStrings("\n", results);
    }
}
