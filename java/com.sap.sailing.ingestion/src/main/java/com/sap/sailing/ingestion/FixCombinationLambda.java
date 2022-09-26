package com.sap.sailing.ingestion;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.TimedComparator;
import com.sap.sailing.server.gateway.deserialization.impl.FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixMovingJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixMovingJsonSerializer;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.shared.json.JsonDeserializationException;
import com.sap.sse.shared.json.JsonDeserializer;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;
import software.amazon.awssdk.utils.IoUtils;

public class FixCombinationLambda implements RequestStreamHandler {
    private static final Logger logger = Logger.getLogger(FixCombinationLambda.class.getName());
    private final S3Client s3Client = S3Client.builder().region(Configuration.S3_REGION).build();
    private final JsonDeserializer<Pair<UUID, List<GPSFixMoving>>> deserializer = new FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer();
    private final GPSFixMovingJsonDeserializer deserializerNoUUID = new GPSFixMovingJsonDeserializer();

    @Override
    public void handleRequest(final InputStream input, final OutputStream output, final Context context) {
        try {
            logger.info("FixCombination Lambda is starting");
            Map<String, List<S3Object>> allNewFixMetadataFromDevices = this.getMetadataOfNewFixes();
            ArrayList<ObjectIdentifier> keysToDelete = new ArrayList<ObjectIdentifier>();
            Map<String, TreeSet<GPSFixMoving>> allFixDataFromDevices = this
                    .getDataOfNewFixes(allNewFixMetadataFromDevices, keysToDelete);
            this.divideFixesAndCreateCollections(allFixDataFromDevices);
            this.deleteFixesUsedForCollections(keysToDelete);
        } catch (S3Exception e) {
            logger.log(Level.SEVERE, e.awsErrorDetails().errorMessage());
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

    private Map<String, List<S3Object>> getMetadataOfNewFixes() {
        final ListObjectsV2Request listNewFixesRequest = ListObjectsV2Request.builder()
                .bucket(Configuration.S3_BUCKET_NAME).prefix("testCombination/").delimiter("/collection/").build();
        final ListObjectsV2Iterable listNewFixes = s3Client.listObjectsV2Paginator(listNewFixesRequest);
        Map<String, List<S3Object>> allNewFixMetadataFromDevices = listNewFixes.stream()
                .flatMap(listNewFixesPage -> listNewFixesPage.contents().stream())
                .filter(newFix -> newFix.key().contains(".json"))
                .collect(Collectors.groupingBy(newFix -> newFix.key().substring(0, newFix.key().lastIndexOf("/"))));
        return allNewFixMetadataFromDevices;
    }

    private Map<String, TreeSet<GPSFixMoving>> getDataOfNewFixes(
            Map<String, List<S3Object>> allNewFixMetadataFromDevices, ArrayList<ObjectIdentifier> keysToDelete) {
        Map<String, TreeSet<GPSFixMoving>> allFixDataFromDevices = new HashMap<String, TreeSet<GPSFixMoving>>();
        allNewFixMetadataFromDevices.entrySet().parallelStream().forEach(newFixMetadataFromDevice -> {
            logger.info("Fetching fixes from: " + newFixMetadataFromDevice.getKey() + " (Amount: "
                    + Integer.toString(newFixMetadataFromDevice.getValue().size()) + ")");
            allFixDataFromDevices.put(newFixMetadataFromDevice.getKey(), new TreeSet<>(new TimedComparator()));
            newFixMetadataFromDevice.getValue().parallelStream().forEach(newFix -> {
                GetObjectRequest getDataForSingleFixRequest = GetObjectRequest.builder()
                        .bucket(Configuration.S3_BUCKET_NAME).key(newFix.key()).build();
                try {
                    final Object newFixObject = JSONValue.parseWithException(
                            FixCombinationLambda.loadJSONFromGetRequest(s3Client, getDataForSingleFixRequest));
                    final JSONObject newFixJson = Helpers.toJSONObjectSafe(newFixObject);
                    final Pair<UUID, List<GPSFixMoving>> data = deserializer.deserialize(newFixJson);
                    final List<GPSFixMoving> fixes = data.getB();
                    for (GPSFixMoving fix : fixes) {
                        if (this.moreThanTenMinutesAgo(fix)) {
                            allFixDataFromDevices.get(newFixMetadataFromDevice.getKey()).add(fix);
                            final ObjectIdentifier fixToDelete = ObjectIdentifier.builder().key(newFix.key()).build();
                            keysToDelete.add(fixToDelete);
                        }
                    }
                } catch (ParseException | JsonDeserializationException e) {
                    logger.log(Level.SEVERE,
                            "Exception for key " + newFix.key() + " with JSON operations: " + e.getMessage());
                } catch (IOException e) {
                    logger.log(Level.SEVERE,
                            "Exception for key " + newFix.key() + " while receiving object from S3: " + e.getMessage());
                }
            });
        });
        logger.info("Finished list of objects in S3");
        return allFixDataFromDevices;
    }

    private void divideFixesAndCreateCollections(Map<String, TreeSet<GPSFixMoving>> allFixDataFromDevices) {
        allFixDataFromDevices.entrySet().parallelStream().forEach(fixDataFromDevice -> {
            fixDataFromDevice.getValue().stream().filter(this::moreThanTenMinutesAgo)
                    .collect(Collectors.groupingBy(this::roundToTenMinutes,
                            Collectors.toCollection(() -> new TreeSet<>(new TimedComparator()))))
                    .forEach((timestamp, newFixesTimeSet) -> {
                        String logAboutCollectionProcess = "Device Id: "
                                + String.join("-", fixDataFromDevice.getKey().split("/")) + "\tTimestamp: "
                                + Long.toString(timestamp) + "\tAmount of new Fixes: "
                                + Integer.toString(newFixesTimeSet.size());
                        logAboutCollectionProcess += "\tFixes: " + newFixesTimeSet.toString();
                        TreeSet<GPSFixMoving> fixSetToStore = null;
                        final String keyForCollection = this.generateKeyForCollection(fixDataFromDevice.getKey(),
                                timestamp);
                        final GetObjectRequest existingFixCollectionRequest = GetObjectRequest.builder()
                                .bucket(Configuration.S3_BUCKET_NAME).key(keyForCollection).build();
                        try {
                            final Object fixObject = JSONValue.parseWithException(FixCombinationLambda
                                    .loadJSONFromGetRequest(s3Client, existingFixCollectionRequest));
                            final JSONArray fixJsonArray = Helpers.toJSONArraySafe(fixObject);
                            TreeSet<GPSFixMoving> existingFixes = new TreeSet<>(new TimedComparator());
                            Iterator<Object> fixJsonArrayIterator = fixJsonArray.iterator();
                            while (fixJsonArrayIterator.hasNext()) {
                                final JSONObject fixJsonObject = Helpers.toJSONObjectSafe(fixJsonArrayIterator.next());
                                final GPSFixMoving data = deserializerNoUUID.deserialize(fixJsonObject);
                                existingFixes.add(data);
                            }
                            if (existingFixes.size() > newFixesTimeSet.size()) {
                                existingFixes.addAll(newFixesTimeSet);
                                fixSetToStore = existingFixes;
                            } else {
                                newFixesTimeSet.addAll(existingFixes);
                            }
                            logAboutCollectionProcess += "\tAdded data to existing collection with key "
                                    + keyForCollection;
                        } catch (NoSuchKeyException e) {
                            logAboutCollectionProcess += "\tCollection with key " + keyForCollection
                                    + " does not exist, creating a new one...";
                        } catch (ParseException | JsonDeserializationException e) {
                            logAboutCollectionProcess += "\tDeserializing existing collection for key "
                                    + keyForCollection + " failed: " + e.getMessage();
                        } catch (IOException e) {
                            logAboutCollectionProcess += "\tFetching existing collection for key " + keyForCollection
                                    + " failed: " + e.getMessage();
                        }
                        final PutObjectRequest saveFixCollectionRequest = PutObjectRequest.builder()
                                .bucket(Configuration.S3_BUCKET_NAME).key(keyForCollection).build();
                        if (fixSetToStore == null)
                            fixSetToStore = newFixesTimeSet;
                        JSONArray fixSetToStoreAsJson = new JSONArray();
                        fixSetToStore.stream().map((object) -> new GPSFixMovingJsonSerializer().serialize(object))
                                .forEach(fixSetToStoreAsJson::add);
                        s3Client.putObject(saveFixCollectionRequest,
                                RequestBody.fromString(fixSetToStoreAsJson.toJSONString()));
                        logger.info(logAboutCollectionProcess);
                    });
        });
        logger.info("Finished combination of fixes into collection files");
    }

    private void deleteFixesUsedForCollections(ArrayList<ObjectIdentifier> keysToDelete) {
        logger.info("Keys to delete: " + keysToDelete.toString());
        if (keysToDelete.size() != 0) {
            final Delete deleteFixes = Delete.builder().objects(keysToDelete).build();
            final DeleteObjectsRequest deleteFixesRequest = DeleteObjectsRequest.builder()
                    .bucket(Configuration.S3_BUCKET_NAME).delete(deleteFixes).build();
            s3Client.deleteObjects(deleteFixesRequest);
            logger.info("Deleted single fix files used for collection");
        }
    }

    private long roundToTenMinutes(GPSFixMoving fixToRound) {
        final Timestamp t = new Timestamp(fixToRound.getTimePoint().asMillis());
        final long fixTime = t.getTime();
        t.setTime(fixTime - (fixTime % 600_000));
        return t.getTime();
    }

    private Boolean moreThanTenMinutesAgo(GPSFixMoving fixToCheck) {
        final long t = fixToCheck.getTimePoint().asMillis();
        return t + 600_000 < System.currentTimeMillis();
    }

    private String generateKeyForCollection(String deviceKey, long timestampOfFix) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final Date d = new Date(timestampOfFix);
        return deviceKey + "/collection/" + sdf.format(d) + "-UTC.json";
    }

    protected static String loadJSONFromGetRequest(S3Client s3Client, GetObjectRequest getObjectRequest)
            throws IOException {
        final ResponseInputStream<GetObjectResponse> fixData = s3Client.getObject(getObjectRequest);
        final String strFixData = IoUtils.toUtf8String(fixData);
        fixData.close();
        return strFixData;
    }
}
