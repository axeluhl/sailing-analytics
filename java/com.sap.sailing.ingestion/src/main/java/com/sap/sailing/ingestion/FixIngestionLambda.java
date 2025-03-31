package com.sap.sailing.ingestion;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.redisson.api.RMap;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneUUIDIdentifierImpl;
import com.sap.sailing.ingestion.dto.EndpointDTO;
import com.sap.sailing.server.gateway.deserialization.impl.FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixMovingJsonSerializer;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.shared.json.JsonDeserializationException;
import com.sap.sse.shared.json.JsonDeserializer;
import com.sap.sse.shared.json.JsonSerializer;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * This λ accepts fixes that adhere to the structure {@link Pair<UUID, List<GPSFixMoving>>} which are provided in the
 * body of an AWS request which is parsed with the {@link AWSInOutHandler}. In most cases clients will want to submit
 * GPS fixes thus adhering to the {@link GPSFixMoving} structure. This structure will be recognized by most sailing
 * servers.
 */

public class FixIngestionLambda implements RequestStreamHandler {
    private final AWSInOutHandler awsInOut = new AWSInOutHandler();
    private final JsonDeserializer<Pair<UUID, List<GPSFixMoving>>> gpsFixDeserializer = new FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer();
    private final JsonSerializer<GPSFixMoving> gpsFixSerializer = new GPSFixMovingJsonSerializer();
    private final S3FixStorageStructure s3FixStorageStructure = new S3FixStorageStructure();
    private final S3Client s3Client = S3Client.builder().region(Configuration.S3_REGION).build();
    private final RMap<String, List<EndpointDTO>> cacheMap = RedisUtils.getCacheMap();
    private static final Logger logger = Logger.getLogger(FixIngestionLambda.class.getName());

    @Override
    public void handleRequest(final InputStream inputAsStream, final OutputStream outputAsStream, final Context context) {
        try {
            final JSONObject requestObject = awsInOut.parseInputToJson(inputAsStream);
            logger.info("Input: " + requestObject.toJSONString());
            final Pair<UUID, List<GPSFixMoving>> data = gpsFixDeserializer.deserialize(requestObject);
            final DeviceIdentifier deviceIdentifier = new SmartphoneUUIDIdentifierImpl(data.getA());
            final List<GPSFixMoving> newFixes = data.getB();
            final byte[] bodyAsBytes = requestObject.toJSONString().getBytes();
            final ForkJoinPool dispatchToSubscribersTask = ForkJoinPool.commonPool();
            dispatchToSubscribersTask.submit(() -> {
                try {
                    storeFixFileToS3(deviceIdentifier, newFixes);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Exception trying to store fixes to S3: " + e.getMessage());
                }
            });
            final List<EndpointDTO> listOfEndpointsToTrigger = cacheMap.get(deviceIdentifier.getStringRepresentation());
            if (listOfEndpointsToTrigger != null) {
                for (final EndpointDTO endpoint : listOfEndpointsToTrigger) {
                    dispatchToSubscribersTask.submit(() -> {
                        dispatchToSubscribers(endpoint, bodyAsBytes);
                    });
                }
            } else {
                logger.info("No endpoint has been configured for identifier " + deviceIdentifier.getStringRepresentation());
            }
            // wait for tasks to complete for <number of end-points>*<timeout for connection>+<ramp-up time>
            dispatchToSubscribersTask.awaitQuiescence(
                    (listOfEndpointsToTrigger==null?0:listOfEndpointsToTrigger.size() * Configuration.TIMEOUT_IN_SECONDS_WHEN_DISPATCHING_TO_ENDPOINT) + 2,
                    TimeUnit.SECONDS);
            final String successResponse = awsInOut.createJsonResponse(deviceIdentifier.getStringRepresentation()).toJSONString();
            logger.info(successResponse);
            outputAsStream.write(successResponse.getBytes());
        } catch (ParseException | JsonDeserializationException e) {
            logger.log(Level.SEVERE, "Exception trying to deserialize JSON input: " + e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        } finally {
            try {
                inputAsStream.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Exception trying to close input: " + e.getMessage());
            }
            try {
                outputAsStream.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Exception trying to close output: " + e.getMessage());
            }
        }
    }

    private void dispatchToSubscribers(final EndpointDTO endpoint, final byte[] jsonAsBytes) {
        logger.info("Connecting to endpoint " + endpoint.getEndpointCallbackUrl() + " with ID " + endpoint.getEndpointUuid());
        try {
            final URL endpointUrl = new URL(endpoint.getEndpointCallbackUrl());
            try {
                final HttpURLConnection connectionToEndpoint = (HttpURLConnection) endpointUrl.openConnection();
                connectionToEndpoint.setRequestMethod("POST");
                connectionToEndpoint.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connectionToEndpoint.setRequestProperty("Accept", "application/json");
                connectionToEndpoint.setDoOutput(true);
                connectionToEndpoint.addRequestProperty("Content-Length", String.valueOf(jsonAsBytes.length));
                connectionToEndpoint.setConnectTimeout(
                        (int) Duration.ofSeconds(Configuration.TIMEOUT_IN_SECONDS_WHEN_DISPATCHING_TO_ENDPOINT).toMillis());
                try (final OutputStream os = connectionToEndpoint.getOutputStream()) {
                    os.write(jsonAsBytes);
                    os.flush();
                    final int responseCode = connectionToEndpoint.getResponseCode(); // reading is important to actually issue the request
                    logger.info("Sent data " + new String(jsonAsBytes) + " to " + endpoint.getEndpointCallbackUrl() + " with response code " + responseCode);
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Exception trying to send data to " + endpointUrl + ": " + ex.getMessage());
            }
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, "Malformed URL for end point " + endpoint.getEndpointCallbackUrl());
        }
    }

    private void storeFixFileToS3(final DeviceIdentifier deviceIdentifier, final List<GPSFixMoving> newFixes)
            throws IOException {
        try {
            final String dataAsString = newFixes.toString();
            logger.info("Data to write: " + dataAsString);
            for (GPSFixMoving fix : newFixes) {
                final String destinationKey = s3FixStorageStructure.generateKeyForSingleFix(deviceIdentifier, fix.getTimePoint());
                logger.info("Location: " + destinationKey);
                final PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(Configuration.S3_BUCKET_NAME)
                        .key(destinationKey).contentType("application/json").build();
                final JSONObject serializedFix = gpsFixSerializer.serialize(fix);
                s3Client.putObject(putObjectRequest, RequestBody.fromString(serializedFix.toJSONString()));
            }
            logger.info("Finished putting object into S3");
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Error writing GPS fix to S3: "+e.getMessage(), e);
            throw e;
        }
    }
}
