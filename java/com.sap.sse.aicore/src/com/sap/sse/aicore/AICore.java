package com.sap.sse.aicore;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;

import com.sap.sse.aicore.impl.AICoreImpl;
import com.sap.sse.common.Util;

public interface AICore {
    static AICore create(final Credentials credentials) {
        return new AICoreImpl(credentials);
    }
    
    Iterable<Deployment> getDeployments() throws UnsupportedOperationException, ClientProtocolException, URISyntaxException, IOException, ParseException;
    
    default Optional<Deployment> getDeploymentByModelName(String modelName) throws UnsupportedOperationException, ClientProtocolException, URISyntaxException, IOException, ParseException {
        return Util.stream(getDeployments()).filter(d->d.getModelName().equals(modelName)).findAny();
    }
}
