package com.sap.sailing.selenium.api.core;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

public class ApiContext {

    private static final Logger logger = Logger.getLogger(ApiContext.class.getName());

    protected final Client client;
    protected final String token;
    private final String contextRoot;
    private final String context;

    private ApiContext(String contextRoot, String context, String token) {
        this.contextRoot = contextRoot;
        this.token = token;
        this.context = context;
        client = new Client();
    }

    public static ApiContext createApiContext(String contextRoot, String context, String username, String password) {
        Authenticator authenticator = new Authenticator(contextRoot);
        String token = authenticator.authForToken(username, password);
        return new ApiContext(contextRoot, context, token);
    }

    private WebResource getWebResource() {
        logger.info("creating web resource for " + context);
        return client.resource(contextRoot + context);
    }

    public JSONObject post(String url, Map<String, String> queryParams, Map<String, String> formParams) {
        WebResource wres = getWebResource().path(url);
        Form form = new Form();
        if (formParams != null) {
            formParams.forEach(form::putSingle);
        }
        String result;
        try {
            if (queryParams != null) {
                for (Entry<String, String> e : queryParams.entrySet()) {
                    if (e.getKey() != null && e.getValue() != null) {
                        wres = wres.queryParam(e.getKey(), e.getValue());
                    }
                }
            }
            result = wres.getRequestBuilder().header("Authorization", "Bearer " + token).entity(form)
                    .post(String.class);
        } catch (UniformInterfaceException e) {
            logger.severe("API POST request " + url + " failed (rc=" + e.getResponse().getStatus() + "): "
                    + e.getResponse().getEntity(String.class));
            throw new RuntimeException(e.getResponse().getEntity(String.class));
        }
        return (JSONObject) JSONValue.parse(result);
    }

    public JSONObject put(String url, Map<String, String> queryParams, JSONObject body) {
        WebResource wres = getWebResource().path(url);
        String result;
        try {
            if (queryParams != null) {
                for (Entry<String, String> e : queryParams.entrySet()) {
                    wres = wres.queryParam(e.getKey(), e.getValue());
                }
            }
            result = wres.getRequestBuilder().header("Authorization", "Bearer " + token)
                    .entity(body.toJSONString(), MediaType.APPLICATION_JSON).put(String.class);
        } catch (UniformInterfaceException e) {
            String error = e.getResponse().getEntity(String.class);
            logger.severe("API PUT request " + url + " failed (rc=" + e.getResponse().getStatus() + "): " + error);
            throw new RuntimeException(error);
        }
        return (JSONObject) JSONValue.parse(result);
    }

    public void delete(String url) {
        WebResource wres = getWebResource().path(url);
        try {
            wres.getRequestBuilder().header("Authorization", "Bearer " + token).delete();
        } catch (UniformInterfaceException e) {
            logger.severe("API PUT request " + url + " failed (rc=" + e.getResponse().getStatus() + "): "
                    + e.getResponse().getEntity(String.class));
            throw new RuntimeException(e.getResponse().getEntity(String.class));
        }
    }

    public JSONObject get(String url) {
        return (JSONObject) getObject(url);
    }

    public JSONArray getList(String url) {
        return (JSONArray) getObject(url);
    }

    private Object getObject(String url) {
        String result;
        WebResource wres = getWebResource().path(url);
        try {
            result = wres.getRequestBuilder().header("Authorization", "Bearer " + token).get(String.class);
        } catch (UniformInterfaceException e) {
            int rc = e.getResponse().getStatus();
            if (rc == 204) {
                logger.info("API GET request " + url + " failed (rc=" + e.getResponse().getStatus() + "): "
                        + "<no content>");
                return null;
            } else {
                logger.severe("API GET request " + url + " failed (rc=" + e.getResponse().getStatus() + "): "
                        + e.getResponse().getEntity(String.class));
                throw new RuntimeException(e.getResponse().getEntity(String.class));
            }

        }
        return JSONValue.parse(result);
    }

}