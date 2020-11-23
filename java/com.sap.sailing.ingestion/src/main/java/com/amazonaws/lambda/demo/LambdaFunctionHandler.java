package com.amazonaws.lambda.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import net.spy.memcached.MemcachedClient;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class LambdaFunctionHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    private final String destinationBucket = "TODOSomeBucketForAllOurFixes-per-region?";

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        context.getLogger().log("Input: " + input);
        context.getLogger().log("Input is of type "+input.getClass().getName());
        Map<String,Object> responseMap = new HashMap<>();
        responseMap.put("statusCode", 200);
        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Cache-Control", "no-store");
        responseMap.put("headers", headers);
        responseMap.put("isBase64Encoded", Boolean.FALSE);
        responseMap.put("body", "<h1>Hello from Lambda!</h1>");
        context.getLogger().log("Response: "+responseMap);
        final String body = (String) input.get("body");
        // launch dispatching to subscribers in background to reduce response times
        final ForkJoinTask<?> dispatchToSubscribersTask = ForkJoinPool.commonPool().submit(()->{
            try {
                dispatchToSubscribers(body);
            } catch (IOException e) {
                context.getLogger().log(e.getMessage());
            }
        });
        final S3Client s3Client = S3Client.create();
        final String destinationKey = getDestinationKey(body);
        final PutObjectRequest putObjectRequest =
                PutObjectRequest.builder()
                    .bucket(destinationBucket)
                    .key(destinationKey)
                    .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(body.getBytes()));
        // wait for dispatching to complete:
        dispatchToSubscribersTask.join();
        return responseMap;
    }

    private void dispatchToSubscribers(String body) throws IOException {
        final MemcachedClient memcachedClient = new MemcachedClient((InetSocketAddress) null); // TODO
        final Object subscriptions = memcachedClient.get(getDeviceIdentifier(body));
        // TODO Implement LambdaFunctionHandler.dispatchToSubscribers(...)
    }

    private String getDeviceIdentifier(String body) {
        // TODO Implement LambdaFunctionHandler.getDeviceIdentifier(...)
        return null;
    }

    private String getDestinationKey(String body) {
        // TODO Implement LambdaFunctionHandler.getDestinationKey(...)
        return null;
    }

}
