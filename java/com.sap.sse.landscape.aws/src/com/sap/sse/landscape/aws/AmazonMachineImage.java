package com.sap.sse.landscape.aws;

import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;

import software.amazon.awssdk.services.ec2.model.BlockDeviceMapping;
import software.amazon.awssdk.services.ec2.model.ImageState;

public interface AmazonMachineImage<ShardingKey, MetricsT extends ApplicationProcessMetrics> extends MachineImage {
     Iterable<BlockDeviceMapping> getBlockDeviceMappings();

    @Override
    String getId();

    ImageState getState();
}
