package com.sap.sailing.ingestion;

import java.util.ArrayList;
import java.util.List;

import org.redisson.api.RMap;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.sap.sailing.ingestion.dto.AWSIngestionWrapper;
import com.sap.sailing.ingestion.dto.AWSResponseWrapper;
import com.sap.sailing.ingestion.dto.EndpointDTO;

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
public class EndpointRegistrationLambda implements RequestHandler<AWSIngestionWrapper<EndpointDTO>, String> {
    @SuppressWarnings("unchecked")
    @Override
    public String handleRequest(AWSIngestionWrapper<EndpointDTO> awsWrappedInput, Context context) {
        final EndpointDTO input = awsWrappedInput.getBody();
        if (input != null && input.getDevicesUuid() != null && input.getDevicesUuid().size() > 0) {
            context.getLogger().log("Getting cache instance...");
            final RMap<String, List<EndpointDTO>> cacheMap = Utils.getCacheMap();
            context.getLogger().log("Got cache instance");
            for (final String deviceUuid : input.getDevicesUuid()) {
                final Object memObject = cacheMap.get(deviceUuid);
                final List<EndpointDTO> endpoints = memObject == null ? new ArrayList<>()
                        : (List<EndpointDTO>) memObject;
                if (input.isRegisterAction()) {
                    endpoints.add(input);
                    context.getLogger().log("Added endpoint for device UUID " + deviceUuid + " with url "
                            + input.getEndpointCallbackUrl());
                } else if (input.isUnRegisterAction()) {
                    endpoints.remove(input);
                }
                cacheMap.put(deviceUuid, endpoints);
            }
        }
        return new Gson().toJson(AWSResponseWrapper.successResponseAsJson(input.getEndpointUuid()));
    }
}
