package com.sap.sse.landscape.aws.impl;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.sap.sse.common.TimePoint;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.Region;

import software.amazon.awssdk.services.ec2.model.Image;

public class AmazonMachineImage implements MachineImage<AwsInstance> {
    private final Image image;
    private final Region region;
    
    public AmazonMachineImage(Image image, Region region) {
        this.image = image;
        this.region = region;
    }

    @Override
    public Serializable getId() {
        return image.imageId();
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
    public AmazonMachineImage updateAllPackages() {
        // TODO Implement MachineImage<AwsInstance>.updateAllPackages(...)
        return null;
    }

    @Override
    public void delete() {
        // TODO Implement MachineImage<AwsInstance>.delete(...)
        
    }
}
