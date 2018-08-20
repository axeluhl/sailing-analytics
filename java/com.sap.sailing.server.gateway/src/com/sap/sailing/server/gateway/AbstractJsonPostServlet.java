package com.sap.sailing.server.gateway;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public abstract class AbstractJsonPostServlet<RequestT, ResponseT> extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = 8474205424939759851L;
    private final List<String> requiredParameters;
    private final ArrayList<String> optionalParameters;
    
    public abstract JsonDeserializer<RequestT> getRequestDeserializer();
    public abstract JsonSerializer<ResponseT> getResponseSerializer();

    public AbstractJsonPostServlet(List<String> requiredParameters,
            List<String> optionalParameters) {
        super();
        this.requiredParameters = new ArrayList<>(requiredParameters);
        this.optionalParameters = new ArrayList<>(optionalParameters);
    }

    public AbstractJsonPostServlet(List<String> requiredParameters) {
        this(requiredParameters, Collections.<String> emptyList());
    }

    public AbstractJsonPostServlet() {
        this(Collections.<String> emptyList(), Collections.<String> emptyList());
    }

    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    public abstract ResponseT process(Map<String, String> parameterValues, RequestT domainObject)
            throws HttpExceptionWithMessage;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> parameterValues = new HashMap<>();
        for (String name : requiredParameters) {
            String value = req.getParameter(name);
            if (value == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, String.format("Missing parameter '%s'.", name));
                return;
            }
            parameterValues.put(name, value);
        }
        for(String name : optionalParameters){
            parameterValues.put(name, req.getParameter(name));
        }

        RequestT domainObject = null;

        if (getRequestDeserializer() != null) {
            try {
                logger.fine("Post issued to " + this.getClass().getName());
                Object requestBody = JSONValue.parseWithException(req.getReader());
                JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);
                logger.fine("JSON requestObject is: " + requestObject.toString());

                domainObject = getRequestDeserializer().deserialize(requestObject);
            } catch (ParseException | JsonDeserializationException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        String.format("Invalid JSON in request body:\n%s", e));
                logger.warning(String.format("Exception while parsing post request:\n%s", e.toString()));
                e.printStackTrace();
                return;
            }
        }

        ResponseT responseObject = null;
        try {
            responseObject = process(parameterValues, domainObject);
        } catch (HttpExceptionWithMessage e) {
            e.sendError(resp);
            logger.warning(String.format("Exception while processing post request:\n%s", e.toString()));
            e.printStackTrace();
            return;
        }

        setJsonResponseHeader(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
        if (getResponseSerializer() != null && responseObject != null) {
            resp.getWriter().write(getResponseSerializer().serialize(responseObject).toJSONString());
            logger.fine(String.format("Created %s as result of gateway request", responseObject.toString()));
        }
    }
}
