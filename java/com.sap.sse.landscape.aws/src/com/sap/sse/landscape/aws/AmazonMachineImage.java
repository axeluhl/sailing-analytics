package com.sap.sse.landscape.aws;

import com.sap.sse.landscape.MachineImage;

import software.amazon.awssdk.services.ec2.model.BlockDeviceMapping;
import software.amazon.awssdk.services.ec2.model.ImageState;
import software.amazon.awssdk.services.ec2.model.Tag;

public interface AmazonMachineImage<ShardingKey> extends MachineImage {
    Iterable<BlockDeviceMapping> getBlockDeviceMappings();

    @Override
    String getId();

    ImageState getState();

    Iterable<Tag> getTags();
}
