package com.sap.sse.landscape.aws.impl;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.sap.sse.common.TimePoint;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.AmazonMachineImage;

import software.amazon.awssdk.services.ec2.model.BlockDeviceMapping;
import software.amazon.awssdk.services.ec2.model.Image;

public class AmazonMachineImageImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics> implements AmazonMachineImage<ShardingKey, MetricsT> {
    private static final long serialVersionUID = 1615200981492476022L;
    private final Image image;
    private final Region region;
    
    public AmazonMachineImageImpl(Image image, Region region) {
        this.image = image;
        this.region = region;
    }

    @Override
    public Serializable getId() {
        return image.imageId();
    }

    @Override
    public String getName() {
        return image.name();
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public TimePoint getCreatedAt() {
        Date date;
        try {
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = dateFormat.parse(image.creationDate());
        } catch (ParseException e) {
            date = null;
        }
        return date == null ? null : TimePoint.of(date);
    }

    @Override
    public AmazonMachineImage<ShardingKey, MetricsT> updateAllPackages() {
        // launch with "image-upgrade" as the only user data, then produce the new image
        // TODO Implement MachineImage<AwsInstance>.updateAllPackages(...)
        return null;
    }

    @Override
    public void delete() {
        // TODO implement AmazonMachineImageImpl.delete(); we probably want landscape here...
    }
    
    @Override
    public Iterable<BlockDeviceMapping> getBlockDeviceMappings() {
        return image.blockDeviceMappings();
    }
}
