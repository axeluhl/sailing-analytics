package com.sap.sailing.ingestion;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RMap;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.sap.sailing.ingestion.dto.AWSRequestWrapper;
import com.sap.sailing.ingestion.dto.AWSResponseWrapper;
import com.sap.sailing.ingestion.dto.EndpointDTO;
import com.sap.sailing.ingestion.dto.FixHeaderDTO;
import com.sap.sailing.ingestion.dto.GpsFixPayloadDTO;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.IoUtils;

/**
 * This λ accepts fixes of any kind that adhere to {@link FixHeaderDTO} structure wrapped inside an
 * {@link AWSRequestWrapper}. In most cases clients will want to submit GPS fixes thus adhering to the
 * {@link GpsFixPayloadDTO} structure. This structure will be recognized by most sailing servers.
 */
public class FixIngestionLambda implements RequestStreamHandler {
    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) {
        try {
            final byte[] streamAsBytes = IoUtils.toByteArray(input);
            context.getLogger().log(new String(streamAsBytes));
            final AWSRequestWrapper dtoWrapped = new Gson().fromJson(new String(streamAsBytes), AWSRequestWrapper.class);
            final FixHeaderDTO dto = dtoWrapped.getBodyAsType(FixHeaderDTO.class);
            storeFixFileToS3(dto.getDeviceUuid(), streamAsBytes, context.getLogger());
            final RMap<String, List<EndpointDTO>> cacheMap = Utils.getCacheMap();
            final List<EndpointDTO> listOfEndpointsToTrigger = cacheMap.get(dto.getDeviceUuid());
            if (listOfEndpointsToTrigger != null) {
                final List<EndpointDTO> endpointsToTrigger = listOfEndpointsToTrigger;
                final ForkJoinPool dispatchToSubscribersTask = ForkJoinPool.commonPool();
                for (final EndpointDTO endpoint : endpointsToTrigger) {
                    dispatchToSubscribersTask.submit(() -> {
                        dispatchToSubscribers(context, endpoint, streamAsBytes);
                    });
                }
                // wait for tasks to complete for <number of end-points>*<timeout for connection>+<ramp-up time>
                dispatchToSubscribersTask.awaitQuiescence(
                        (endpointsToTrigger.size() * Configuration.TIMEOUT_IN_SECONDS_WHEN_DISPATCHING_TO_ENDPOINT) + 2,
                        TimeUnit.SECONDS);
            } else {
                context.getLogger().log("No endpoint has been configured for UUID " + dto.getDeviceUuid());
            }
            output.write(new Gson().toJson(AWSResponseWrapper.successResponseAsJson(dto.getDeviceUuid())).getBytes());
            output.close();
        } catch (IOException e) {
            context.getLogger().log(e.getMessage());
        }
    }

    private void dispatchToSubscribers(final Context context, final EndpointDTO endpoint, final byte[] jsonAsBytes) {
        context.getLogger().log("Connecting to endpoint " + endpoint.getEndpointCallbackUrl());
        try {
            final URL endpointUrl = new URL(endpoint.getEndpointCallbackUrl());
            final HttpURLConnection connectionToEndpoint = (HttpURLConnection) endpointUrl.openConnection();
            connectionToEndpoint.setRequestMethod("POST");
            connectionToEndpoint.setRequestProperty("Content-Type", "application/json; utf-8");
            connectionToEndpoint.setRequestProperty("Accept", "application/json");
            connectionToEndpoint.setDoOutput(true);
            connectionToEndpoint.setConnectTimeout(
                    (int) Duration.ofSeconds(Configuration.TIMEOUT_IN_SECONDS_WHEN_DISPATCHING_TO_ENDPOINT).toMillis());
            context.getLogger().log(new String(jsonAsBytes));
            try (final OutputStream os = connectionToEndpoint.getOutputStream()) {
                os.write(jsonAsBytes);
            } 
        } catch (Exception ex) {
            context.getLogger().log(ex.getMessage());
        }
        context.getLogger().log("Sent data to " + endpoint.getEndpointCallbackUrl());
    }

    private void storeFixFileToS3(final String deviceUuid, final byte[] jsonAsBytes, final LambdaLogger logger)
            throws IOException {
        try (final S3Client s3Client = S3Client.builder().region(Configuration.S3_REGION).build()) {
            final String destinationKey = getDestinationKey(deviceUuid);
            final PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(Configuration.S3_BUCKET_NAME)
                    .key(destinationKey).contentType("application/json").build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(jsonAsBytes));
            logger.log("Finished putting object into S3");
        }
    }

    private String getDestinationKey(final String deviceUuid) {
        return getUuidSplitIntoS3Prefixes(deviceUuid) + "/" + LocalDateTime.now().toString() + ".json";
    }

    private String getUuidSplitIntoS3Prefixes(final String uuid) {
        return String.join("/", uuid.split("-"));
    }
}
