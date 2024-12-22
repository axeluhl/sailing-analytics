package com.sap.sailing.domain.igtimiadapter.persistence.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;
import org.bson.types.Binary;

import com.google.protobuf.InvalidProtocolBufferException;
import com.igtimi.IgtimiData.Data;
import com.igtimi.IgtimiData.DataMsg;
import com.igtimi.IgtimiData.DataMsg.Builder;
import com.igtimi.IgtimiData.DataPoint;
import com.igtimi.IgtimiData.DataPoint.DataCase;
import com.igtimi.IgtimiStream.Msg;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sailing.domain.igtimiadapter.persistence.DomainObjectFactory;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private static final Logger logger = Logger.getLogger(DomainObjectFactoryImpl.class.getName());

    private final MongoDatabase db;
    private final MongoCollection<Document> messagesCollection;

    public DomainObjectFactoryImpl(MongoDatabase db) {
        this.db = db;
        messagesCollection = MongoObjectFactoryImpl.getOrCreateMessagesCollection(db);
    }
    
    @Override
    public Iterable<Resource> getResources() {
        final List<Resource> result = new ArrayList<>();
        final MongoCollection<org.bson.Document> resourcesCollection = db.getCollection(CollectionNames.IGTIMI_RESOURCES.name());
        for (Object o : resourcesCollection.find()) {
            final long id = ((Number) ((Document) o).get(FieldNames.IGTIMI_RESOURCES_ID.name())).longValue();
            final TimePoint startTime = getTimePoint((Document) o, FieldNames.IGTIMI_RESOURCES_START_TIME_MILLIS);
            final TimePoint endTime = getTimePoint((Document) o, FieldNames.IGTIMI_RESOURCES_END_TIME_MILLIS);
            final String deviceSerialNumber = (String) ((Document) o).get(FieldNames.IGTIMI_RESOURCES_DEVICE_SERIAL_NUMBER.name());
            final Integer[] dataTypesAsInteger = ((List<?>) ((Document) o).get(FieldNames.IGTIMI_RESOURCES_DATA_TYPES.name())).toArray(new Integer[0]);
            final int[] dataTypes = new int[dataTypesAsInteger.length];
            for (int i=0; i<dataTypesAsInteger.length; i++) {
                dataTypes[i] = dataTypesAsInteger[i];
            }
            result.add(Resource.create(id, startTime, endTime, deviceSerialNumber, dataTypes));
        }
        return result;
    }

    private TimePoint getTimePoint(Document o, final FieldNames timePointFieldName) {
        final Number startTimeMillisNumber = (Number) o.get(timePointFieldName.name());
        final TimePoint startTime = startTimeMillisNumber == null ? null : TimePoint.of(startTimeMillisNumber.longValue());
        return startTime;
    }

    @Override
    public Iterable<DataAccessWindow> getDataAccessWindows() {
        final List<DataAccessWindow> result = new ArrayList<>();
        final MongoCollection<org.bson.Document> devicesCollection = db.getCollection(CollectionNames.IGTIMI_DATA_ACCESS_WINDOWS.name());
        for (Object o : devicesCollection.find()) {
            final Number id = ((Number) ((Document) o).get(FieldNames.IGTIMI_DATA_ACCESS_WINDOWS_ID.name()));
            final TimePoint startTime = getTimePoint((Document) o, FieldNames.IGTIMI_DATA_ACCESS_WINDOWS_START_TIME_MILLIS);
            final TimePoint endTime = getTimePoint((Document) o, FieldNames.IGTIMI_DATA_ACCESS_WINDOWS_END_TIME_MILLIS);
            final String deviceSerialNumber = (String) ((Document) o).get(FieldNames.IGTIMI_DATA_ACCESS_WINDOWS_DEVICE_SERIAL_NUMBER.name());
            result.add(DataAccessWindow.create(id.longValue(), startTime, endTime, deviceSerialNumber));
        }
        return result;
    }

    @Override
    public Iterable<Device> getDevices() {
        final List<Device> result = new ArrayList<>();
        final MongoCollection<org.bson.Document> devicesCollection = db.getCollection(CollectionNames.IGTIMI_DEVICES.name());
        for (Object o : devicesCollection.find()) {
            final Number id = ((Number) ((Document) o).get(FieldNames.IGTIMI_DEVICES_ID.name()));
            final String serialNumber = (String) ((Document) o).get(FieldNames.IGTIMI_DEVICES_SERIAL_NUMBER.name());
            final String name = (String) ((Document) o).get(FieldNames.IGTIMI_DEVICES_NAME.name());
            final String serviceTag = (String) ((Document) o).get(FieldNames.IGTIMI_DEVICES_SERVICE_TAG.name());
            result.add(Device.create(id.longValue(), serialNumber, name, serviceTag));
        }
        return result;
    }

    @Override
    public Iterable<Msg> getMessages(String deviceSerialNumber, TimeRange timeRange, Set<DataCase> dataCases) {
        final Document query = new Document();
        appendTimeRangeQuery(query, timeRange);
        query.append(FieldNames.IGTIMI_MESSAGES_DEVICE_SERIAL_NUMBER.name(), deviceSerialNumber);
        final Iterable<Document> queryResult = messagesCollection.find(query);
        return Util.filter(Util.map(queryResult,
                doc->{
                    try {
                        final Msg msg = Msg.parseFrom(doc.get(FieldNames.IGTIMI_MESSAGES_PROTOBUF_MESSAGE.name(), Binary.class).getData());
                        return filterMessageForDataCases(msg, dataCases);
                    } catch (InvalidProtocolBufferException e) {
                        logger.log(Level.SEVERE, "Error trying to parse an Igtimi message for device "+deviceSerialNumber, e);
                        return null;
                    }
                }), r->r != null);
    }
    
    /**
     * @return {@code null} in case the {@code msg} message does not contain at least one data point whose data case is
     *         in the set of {@code dataCases}; otherwise the stripped-down message that contains only those data points
     *         matching the {@code dataCases}.
     */
    private Msg filterMessageForDataCases(Msg msg, Set<DataCase> dataCases) {
        final com.igtimi.IgtimiStream.Msg.Builder messageBuilder = msg.toBuilder();
        final List<Builder> dataMsgBuilderList = messageBuilder.getDataBuilder().getDataBuilderList();
        for (final Builder b : dataMsgBuilderList) {
            final List<com.igtimi.IgtimiData.DataPoint.Builder> dataPointBuilderList = b.getDataBuilderList();
            final List<com.igtimi.IgtimiData.DataPoint.Builder> dataPointBuilders = new ArrayList<>();
            for (final Iterator<com.igtimi.IgtimiData.DataPoint.Builder> i=dataPointBuilderList.iterator(); i.hasNext(); ) {
                final com.igtimi.IgtimiData.DataPoint.Builder dataPointBuilder = i.next();
                if (dataCases.contains(dataPointBuilder.getDataCase())) {
                    dataPointBuilders.add(dataPointBuilder);
                }
            }
            b.clearData();
            dataPointBuilders.forEach(dpb->b.addData(dpb));
        }
        final Msg result = messageBuilder.build();
        return hasDataPoints(result) ? result : null;
    }

    private boolean hasDataPoints(Msg result) {
        return result.getData().getDataList().stream().filter(dataMsg->dataMsg.getDataCount() > 0).findAny().isPresent();
    }

    @Override
    public Msg getLatestMessage(String deviceSerialNumber, DataCase dataCase) throws InvalidProtocolBufferException {
        final Document query = new Document();
        query.append(FieldNames.IGTIMI_MESSAGES_DEVICE_SERIAL_NUMBER.name(), deviceSerialNumber);
        final Iterable<Document> queryResult = messagesCollection.find(query).sort(Sorts.descending(FieldNames.IGTIMI_MESSAGES_TIMESTAMP.name()));
        for (final Document document : queryResult) {
            final Binary messageBinary = (Binary) document.get(FieldNames.IGTIMI_MESSAGES_PROTOBUF_MESSAGE.name());
            final Msg msg = Msg.parseFrom(messageBinary.getData());
            if (msg.hasData()) {
                final Data data = msg.getData();
                // iterate in reverse order to find the *last* match
                for (final ListIterator<DataMsg> i=data.getDataList().listIterator(data.getDataCount()); i.hasPrevious(); ) {
                    final DataMsg dataMsg = i.previous();
                    for (final ListIterator<DataPoint> j=dataMsg.getDataList().listIterator(dataMsg.getDataCount()); j.hasPrevious(); ) {
                        final DataPoint dataPoint = j.previous();
                        if (dataPoint.getDataCase() == dataCase) {
                            // the message has a data point of the type requested by dataCase; return it
                            return Msg.newBuilder().setData(
                                       Data.newBuilder().addData(
                                           DataMsg.newBuilder().addData(dataPoint)
                                           .build())
                                       .build())
                                   .build();
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private void appendTimeRangeQuery(Document query, TimeRange timeRange) {
        final Document timeRangeQuery = new Document();
        if (timeRange.from() != null) {
            timeRangeQuery.append("$gte", timeRange.from().asDate());
        }
        if (timeRange.to() != null) {
            timeRangeQuery.append("$lt", timeRange.to().asDate());
        }
        if (!timeRangeQuery.isEmpty()) {
            query.append(FieldNames.IGTIMI_MESSAGES_TIMESTAMP.name(), timeRangeQuery);
        }
    }
}
