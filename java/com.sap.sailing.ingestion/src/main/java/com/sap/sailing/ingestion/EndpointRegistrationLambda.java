package com.sap.sailing.ingestion;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.sap.sailing.ingestion.dto.EndpointDTO;

import net.spy.memcached.MemcachedClient;

/**
 * <p>
 * This lambda handles the registration of HTTP endpoints that need to receive GPS fixes for one or more device UUIDs.
 * Once registered, an endpoint will receive fixes that are being transmitted in almost real-time. The transmission will
 * have a timeout of 2 seconds in order to not unnecessarily block the lambda execution.
 * </p>
 * 
 * <p>
 * Registration needs to be refreshed every 24 hours. Registrations older than this timeframe will be dropped.
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
 *     endpointUuid: "UUID of sailing server"
 *     endpointCallbackUrl: "Callback URL including the hostname, in most cases this should be /v1/gps_fixes"
 *     deviceUuids: [
 *         "List of one or more device UUIDs"
 *     ]
 * }
 * </pre>
 *
 */
public class EndpointRegistrationLambda implements RequestHandler<EndpointDTO, String> {
    @SuppressWarnings("unchecked")
    @Override
    public String handleRequest(EndpointDTO input, Context context) {
        if (input != null && input.getDevicesUuid() != null && input.getDevicesUuid().size() > 0) {
            try {
                final MemcachedClient memcachedClient = new MemcachedClient(new InetSocketAddress(
                        Configuration.MEMCACHED_ENDPOINT_HOST, Configuration.MEMCACHED_ENDPOINT_PORT));
                for (String deviceUuid : input.getDevicesUuid()) {
                    Object memObject = memcachedClient.get(deviceUuid);
                    if (memObject == null) {
                        memObject = new ArrayList<EndpointDTO>();
                    }
                    ((List<EndpointDTO>) memObject).add(input);
                    memcachedClient.set(deviceUuid, (int) Duration.ofDays(1).toMinutes() * 60, memObject);
                    context.getLogger().log("Added endpoint for device UUID " + deviceUuid + " with url "
                            + input.getEndpointCallbackUrl());
                }
            } catch (IOException e) {
                context.getLogger().log(e.getMessage());
            }
        }
        return input.getEndpointUuid();
    }
}
