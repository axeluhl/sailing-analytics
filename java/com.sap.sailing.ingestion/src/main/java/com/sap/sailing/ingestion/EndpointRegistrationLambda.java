package com.sap.sailing.ingestion;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.redisson.api.RMap;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.sap.sailing.ingestion.dto.EndpointDTO;
import com.sap.sse.shared.json.JsonDeserializationException;
import com.sap.sse.shared.json.JsonDeserializer;

/**
 * <p>
 * This lambda handles the registration of HTTP endpoints that need to receive GPS fixes for one or more device UUIDs.
 * Once registered, an endpoint will receive fixes that are being transmitted in almost real-time. The transmission will
 * have a timeout of 4 seconds in order to not unnecessarily block the lambda execution.
 * </p>
 * 
 * <p>
 * The protocol of delivery to the registered endpoint will match the
 * com.sap.sailing.server.gateway.jaxrs.api.GPSFixesResource specification.
 * </p>
 * 
 * <p>
 * This lambda expects a JSON of the following structure to be transmitted for registration
 * </p>
 * 
 * <pre>
 * {
 *     body: {
 *      endpointUuid: "UUID of sailing server"
 *      endpointCallbackUrl: "Callback URL including the hostname, in most cases this should be /v1/gps_fixes"
 *      action: "register|unregister",
 *      deviceUuids: [
 *         "List of one or more device UUIDs"
 *      ]
 *     }
 * }
 * </pre>
 *
 */
public class EndpointRegistrationLambda implements RequestStreamHandler {
    private final AWSInOutHandler awsInOut = new AWSInOutHandler();
    private final JsonDeserializer<EndpointDTO> endpointDeserializer = new EndpointJsonDeserializer();

    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(final InputStream inputAsStream, final OutputStream outputAsStream, final Context context) {
        try {
            final JSONObject requestObject = awsInOut.parseInputToJson(inputAsStream);
            context.getLogger().log("Input body: " + requestObject.toJSONString());
            final EndpointDTO input = endpointDeserializer.deserialize(requestObject);
            if (input != null && input.getDevicesUuid() != null && input.getDevicesUuid().size() > 0) {
                final RMap<String, List<EndpointDTO>> cacheMap = RedisUtils.getCacheMap();
                for (final String deviceUuid : input.getDevicesUuid()) {
                    final Object memObject = cacheMap.get(deviceUuid);
                    final List<EndpointDTO> endpoints = memObject == null ? new ArrayList<>() : (List<EndpointDTO>) memObject;
                    if (input.isRegisterAction()) {
                        if (!endpoints.contains(input)) {
                            endpoints.add(input);
                            context.getLogger().log("Added endpoint for device UUID " + deviceUuid + " with url "
                                    + input.getEndpointCallbackUrl() + " and ID " + input.getEndpointUuid());
                        }
                    } else if (input.isUnRegisterAction()) {
                        endpoints.remove(input);
                        context.getLogger().log("Removed endpoint for device UUID " + deviceUuid + " with url "
                                        + input.getEndpointCallbackUrl() + " and ID " + input.getEndpointUuid()
                                        + ". Remaining subscriptions for device UUID: " + endpoints);
                    }
                    cacheMap.put(deviceUuid, endpoints);
                }
            }
            final String successResponse = awsInOut.createJsonResponse("\"" + input.getEndpointUuid() + "\"").toJSONString();
            context.getLogger().log(successResponse);
            outputAsStream.write(successResponse.getBytes());
        } catch (ParseException | JsonDeserializationException e) {
            context.getLogger().log("Exception trying to deserialize JSON input: " + e.getMessage());
        } catch (Exception ex) {
            context.getLogger().log(ex.getMessage());
        } finally {
            try {
                inputAsStream.close();
            } catch (IOException e) {
                context.getLogger().log("Exception trying to close input: " + e.getMessage());
            }
            try {
                outputAsStream.close();
            } catch (IOException e) {
                context.getLogger().log("Exception trying to close output: " + e.getMessage());
            }
        }
    }
}
