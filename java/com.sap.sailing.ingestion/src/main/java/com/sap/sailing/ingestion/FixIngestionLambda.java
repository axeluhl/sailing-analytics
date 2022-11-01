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
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.redisson.api.RMap;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneUUIDIdentifierImpl;
import com.sap.sailing.ingestion.dto.AWSRequestWrapper;
import com.sap.sailing.ingestion.dto.AWSResponseWrapper;
import com.sap.sailing.ingestion.dto.EndpointDTO;
import com.sap.sailing.ingestion.dto.FixHeaderDTO;
import com.sap.sailing.ingestion.dto.GpsFixPayloadDTO;
import com.sap.sailing.server.gateway.deserialization.impl.FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixMovingJsonSerializer;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.shared.json.JsonDeserializationException;
import com.sap.sse.shared.json.JsonDeserializer;
import com.sap.sse.shared.json.JsonSerializer;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.IoUtils;

/**
 * This λ accepts fixes of any kind that adhere to {@link FixHeaderDTO} structure wrapped inside an
 * {@link AWSRequestWrapper}. In most cases clients will want to submit GPS fixes thus adhering to the
 * {@link GpsFixPayloadDTO} structure. This structure will be recognized by most sailing servers.
 */

//TODO remove Gson dependency
public class FixIngestionLambda implements RequestStreamHandler {
    final S3Client s3Client = S3Client.builder().region(Configuration.S3_REGION).build();
    final RMap<String, List<EndpointDTO>> cacheMap = RedisUtils.getCacheMap();
    private final JsonDeserializer<Pair<UUID, List<GPSFixMoving>>> deserializer =
            new FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer();
    private final JsonSerializer<GPSFixMoving> serializer = new GPSFixMovingJsonSerializer();
    private final S3FixStorageStructure s3FixStorageStructure = new S3FixStorageStructure();
    private static final Logger logger = Logger.getLogger(FixIngestionLambda.class.getName());
    
    @Override
    public void handleRequest(final InputStream input, final OutputStream output, final Context context) {
        try {
            logger.info("Starting Lambda");
            final byte[] streamAsBytes = IoUtils.toByteArray(input);
            logger.info("Input: " + new String(streamAsBytes));
            final AWSRequestWrapper dtoWrapped = new Gson().fromJson(new String(streamAsBytes), AWSRequestWrapper.class);
            Object requestBody = JSONValue.parseWithException(dtoWrapped.getBody());
            JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);
            Pair<UUID, List<GPSFixMoving>> data = deserializer.deserialize(requestObject);
            DeviceIdentifier deviceIdentifier = new SmartphoneUUIDIdentifierImpl(data.getA());
            List<GPSFixMoving> newFixes = data.getB();
            final byte[] bodyAsBytes = dtoWrapped.getBody().getBytes();
            final ForkJoinPool dispatchToSubscribersTask = ForkJoinPool.commonPool();
            dispatchToSubscribersTask.submit(() -> {
                try {
                    storeFixFileToS3(deviceIdentifier, newFixes);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Exception trying to store fixes to S3: "+e.getMessage());
                }
            });
            final List<EndpointDTO> listOfEndpointsToTrigger = cacheMap.get(deviceIdentifier.getStringRepresentation());
            if (listOfEndpointsToTrigger != null) {
                final List<EndpointDTO> endpointsToTrigger = listOfEndpointsToTrigger;
                for (final EndpointDTO endpoint : endpointsToTrigger) {
                    dispatchToSubscribersTask.submit(() -> {
                        dispatchToSubscribers(endpoint, bodyAsBytes);
                    });
                }
                // wait for tasks to complete for <number of end-points>*<timeout for connection>+<ramp-up time>
                dispatchToSubscribersTask.awaitQuiescence(
                        (endpointsToTrigger.size() * Configuration.TIMEOUT_IN_SECONDS_WHEN_DISPATCHING_TO_ENDPOINT) + 2,
                        TimeUnit.SECONDS);
            } else {
                logger.info("No endpoint has been configured for Identifier " + deviceIdentifier.getStringRepresentation());
            }
            output.write(new Gson().toJson(AWSResponseWrapper.successResponseAsJson(deviceIdentifier.getStringRepresentation())).getBytes());
        } catch (ParseException | JsonDeserializationException e) {
            logger.log(Level.SEVERE, "Exception trying to deserialize JSON input: " + e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Exception trying to close input: " + e.getMessage());
            }
            try {
                output.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Exception trying to close output: " + e.getMessage());
            }
        }
    }

    private void dispatchToSubscribers(final EndpointDTO endpoint, final byte[] jsonAsBytes) {
        logger.info("Connecting to endpoint " + endpoint.getEndpointCallbackUrl()+" with ID "+endpoint.getEndpointUuid());
        URL endpointUrl;
        try {
            endpointUrl = new URL(endpoint.getEndpointCallbackUrl());
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
                    logger.info("Sent data "+new String(jsonAsBytes)+" to " + endpoint.getEndpointCallbackUrl()+" with response code "+responseCode);
                } 
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Exception trying to send data to "+endpointUrl+": "+ex.getMessage());
            }
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, "Malformed URL for end point "+endpoint.getEndpointCallbackUrl());
        }
    }

    private void storeFixFileToS3(final DeviceIdentifier deviceIdentifier, final List<GPSFixMoving> newFixes)
            throws IOException {
        final String dataAsString = newFixes.toString();
        logger.info("Data to write: "+dataAsString);
        for (GPSFixMoving fix: newFixes) {
            final String destinationKey = s3FixStorageStructure.generateKeyForSingleFix(deviceIdentifier, fix.getTimePoint());
            logger.info("Location: "+destinationKey);
            final PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(Configuration.S3_BUCKET_NAME)
                    .key(destinationKey).contentType("application/json").build();
            final JSONObject serializedFix = serializer.serialize(fix);
            s3Client.putObject(putObjectRequest, RequestBody.fromString(serializedFix.toJSONString()));
        }
        logger.info("Finished putting object into S3");
    }
}
