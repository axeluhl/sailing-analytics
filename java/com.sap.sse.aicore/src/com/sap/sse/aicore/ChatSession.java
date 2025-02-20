package com.sap.sse.aicore;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;

public interface ChatSession {
    ChatSession addSystemPrompt(String prompt);
    ChatSession addPrompt(String prompt);
    String submit() throws UnsupportedOperationException, ClientProtocolException, URISyntaxException, IOException, ParseException;
}
