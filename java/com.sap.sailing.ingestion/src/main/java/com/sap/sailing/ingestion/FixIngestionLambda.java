package com.sap.sailing.ingestion;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.sap.sailing.ingestion.dto.EndpointDTO;
import com.sap.sailing.ingestion.dto.GpsFixDTO;

import net.spy.memcached.MemcachedClient;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class FixIngestionLambda implements RequestHandler<GpsFixDTO, String> {

	@Override
	public String handleRequest(GpsFixDTO input, Context context) {
		try {
			storeFixFileToS3(input, context.getLogger());
			context.getLogger().log("Connecting to memcached " + Configuration.MEMCACHED_ENDPOINT_HOST);
			final MemcachedClient memcachedClient = new MemcachedClient(new InetSocketAddress(
					Configuration.MEMCACHED_ENDPOINT_HOST, Configuration.MEMCACHED_ENDPOINT_PORT));
			context.getLogger().log("Getting data for device uuid " + input.getDeviceUuid());
			Object listOfEndpointsToTrigger = memcachedClient.get(input.getDeviceUuid());
			if (listOfEndpointsToTrigger != null) {
				context.getLogger().log("Connecting to endpoints");
				@SuppressWarnings("unchecked")
				List<EndpointDTO> endpointsToTrigger = (List<EndpointDTO>) listOfEndpointsToTrigger;
				for (EndpointDTO endpoint : endpointsToTrigger) {
					context.getLogger().log("Connecting to endpoint " + endpoint.getEndpointCallbackUrl());
					URL endpointUrl = new URL(endpoint.getEndpointCallbackUrl());
					HttpURLConnection connectionToEndpoint = (HttpURLConnection) endpointUrl.openConnection();
					connectionToEndpoint.setRequestMethod("POST");
					connectionToEndpoint.setRequestProperty("Content-Type", "application/json; utf-8");
					connectionToEndpoint.setRequestProperty("Accept", "application/json");
					connectionToEndpoint.setDoOutput(true);
					connectionToEndpoint.setConnectTimeout((int) Duration.ofSeconds(2).toMillis());
					final byte[] jsonAsBytes = new Gson().toJson(input).getBytes();
					try (OutputStream os = connectionToEndpoint.getOutputStream()) {
						os.write(jsonAsBytes);
					}
				}
			}

		} catch (IOException e) {
			context.getLogger().log(e.getMessage());
		}
		return input.getDeviceUuid();
	}

	private void storeFixFileToS3(GpsFixDTO input, LambdaLogger logger) {
		try (S3Client s3Client = S3Client.builder().region(Configuration.S3_REGION).build()) {
			final String destinationKey = getDestinationKey(input);
			final PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(Configuration.S3_BUCKET_NAME)
					.key(destinationKey).contentType("application/json").build();
			final byte[] jsonAsBytes = new Gson().toJson(input).getBytes();
			logger.log("Putting object into S3 with " + putObjectRequest.toString());
			s3Client.putObject(putObjectRequest, RequestBody.fromBytes(jsonAsBytes));
			logger.log("Finished putting object into S3");
		}
	}

	private String getDestinationKey(GpsFixDTO input) {
		return getUuidSplitIntoS3Prefixes(input.getDeviceUuid()) + "/" + LocalDateTime.now().toString() + ".json";
	}

	private String getUuidSplitIntoS3Prefixes(String uuid) {
		return String.join("/", uuid.split("-"));
	}

}
