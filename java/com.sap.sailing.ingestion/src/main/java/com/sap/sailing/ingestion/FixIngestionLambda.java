package com.sap.sailing.ingestion;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.redisson.api.RMap;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.sap.sailing.ingestion.dto.EndpointDTO;
import com.sap.sailing.ingestion.dto.FixHeaderDTO;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.IoUtils;

public class FixIngestionLambda implements RequestStreamHandler {
    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) {
        try {
            final byte[] streamAsBytes = IoUtils.toByteArray(input);
            FixHeaderDTO dto = new Gson().fromJson(new String(streamAsBytes), FixHeaderDTO.class);
            storeFixFileToS3(dto.getDeviceUuid(), streamAsBytes, context.getLogger());
            context.getLogger().log("Getting data for device uuid " + dto.getDeviceUuid());
            RMap<String, List<EndpointDTO>> cacheMap = Utils.getCacheMap();
            final List<EndpointDTO> listOfEndpointsToTrigger = cacheMap.get(dto.getDeviceUuid());
            if (listOfEndpointsToTrigger != null) {
                context.getLogger().log("Connecting to endpoints");
                final List<EndpointDTO> endpointsToTrigger = listOfEndpointsToTrigger;
                for (final EndpointDTO endpoint : endpointsToTrigger) {
                    context.getLogger().log("Connecting to endpoint " + endpoint.getEndpointCallbackUrl());
                    final URL endpointUrl = new URL(endpoint.getEndpointCallbackUrl());
                    final HttpURLConnection connectionToEndpoint = (HttpURLConnection) endpointUrl.openConnection();
                    connectionToEndpoint.setRequestMethod("POST");
                    connectionToEndpoint.setRequestProperty("Content-Type", "application/json; utf-8");
                    connectionToEndpoint.setRequestProperty("Accept", "application/json");
                    connectionToEndpoint.setDoOutput(true);
                    connectionToEndpoint.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
                    final byte[] jsonAsBytes = new Gson().toJson(input).getBytes();
                    try (final OutputStream os = connectionToEndpoint.getOutputStream()) {
                        os.write(jsonAsBytes);
                    }
                }
            } else {
                context.getLogger().log("No endpoint has been configured for UUID " + dto.getDeviceUuid());
            }
        } catch (IOException e) {
            context.getLogger().log(e.getMessage());
        }
    }

    private void storeFixFileToS3(String deviceUuid, byte[] jsonAsBytes, LambdaLogger logger) throws IOException {
        try (final S3Client s3Client = S3Client.builder().region(Configuration.S3_REGION).build()) {
            final String destinationKey = getDestinationKey(deviceUuid);
            final PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(Configuration.S3_BUCKET_NAME)
                    .key(destinationKey).contentType("application/json").build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(jsonAsBytes));
            logger.log("Finished putting object into S3");
        }
    }

    private String getDestinationKey(String deviceUuid) {
        return getUuidSplitIntoS3Prefixes(deviceUuid) + "/" + LocalDateTime.now().toString() + ".json";
    }

    private String getUuidSplitIntoS3Prefixes(String uuid) {
        return String.join("/", uuid.split("-"));
    }
}
