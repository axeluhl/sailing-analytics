package com.sap.sse.aicore.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.sap.sse.aicore.AICore;
import com.sap.sse.aicore.ChatSession;
import com.sap.sse.aicore.Credentials;
import com.sap.sse.aicore.Deployment;

public class ChatSessionImpl implements ChatSession {
    private final static String CHAT_PATH_TEMPLATE = "/v2/inference/deployments/%s/chat/completions?api-version=2024-06-01";
    private final List<String> systemPrompts;
    private final List<String> userPrompts;
    private final Credentials credentials;
    private final AICore aiCore;
    private Deployment deployment;

    public ChatSessionImpl(final Credentials credentials, final String modelName) throws UnsupportedOperationException,
            ClientProtocolException, URISyntaxException, IOException, ParseException {
        this.credentials = credentials;
        this.systemPrompts = new ArrayList<>();
        this.userPrompts = new ArrayList<>();
        this.aiCore = new AICoreImpl(credentials);
        this.deployment = aiCore.getDeploymentByModelName(modelName).get();
    }
    
    @Override
    public void addSystemPrompt(String prompt) {
        systemPrompts.add(prompt);
    }

    @Override
    public void addPrompt(String prompt) {
        userPrompts.add(prompt);
    }
    
    private String getChatPath() {
        return String.format(CHAT_PATH_TEMPLATE, deployment.getId());
    }

    @Override
    public String submit() throws UnsupportedOperationException, ClientProtocolException, URISyntaxException, IOException, ParseException {
        final JSONObject chatResponse = credentials.getJSONResponse(getChatPath());
        // TODO place prompts in something like
        // "messages": [
//        {
//            "role": "user",
//            "content": "Hello!"
//          }
//        ]}
        // read results from something like choices[].message.content
        return null;
    }

}
