package com.sap.sse.landscape.aws.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.sap.sse.landscape.AvailabilityZone;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.Log;
import com.sap.sse.landscape.Metrics;
import com.sap.sse.landscape.Process;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.SecurityGroup;

import software.amazon.awssdk.services.ec2.model.Instance;

public class AwsInstance implements Host {
    private final Instance instance;
    private final Region region;
    
    public AwsInstance(Instance instance, Region region) {
        this.instance = instance;
        this.region = region;
    }

    @Override
    public InetAddress getAddress() {
        try {
            return InetAddress.getByName(instance.publicIpAddress());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    
    public com.jcraft.jsch.Session createRootSshSession(KeyPair sshKeyPair) throws JSchException {
        final JSch jsch = new JSch();
        jsch.addIdentity((String) /* TODO identity */ null, (byte[]) /* TODO passphrase */ null);
        return jsch.getSession(getAddress().toString());
    }

    @Override
    public Iterable<? extends Process<? extends Log, ? extends Metrics>> getRunningProcesses() {
        // TODO Implement AwsInstance.getRunningProcesses(...)
        return null;
    }

    @Override
    public long getPhysicalRamInBytes() {
        // TODO Implement AwsInstance.getPhysicalRamInBytes(...)
        return 0;
    }

    @Override
    public long getVirtualMemoryInBytes() {
        // TODO Implement AwsInstance.getVirtualMemoryInBytes(...)
        return 0;
    }

    @Override
    public int getNumberOfCPUs() {
        return instance.cpuOptions().coreCount();
    }

    @Override
    public long getNetworkBandwidthInBytesPerSecond() {
        // TODO Implement AwsInstance.getNetworkBandwidthInBytesPerSecond(...)
        return 0;
    }

    @Override
    public AvailabilityZone getAvailabilityZone() {
        return new AwsAvailabilityZone(instance.placement().availabilityZone(), getRegion());
    }

    private Region getRegion() {
        return region;
    }

    @Override
    public Iterable<SecurityGroup> getSecurityGroups() {
        // TODO Implement AwsInstance.getSecurityGroups(...)
        return null;
    }

    public String getInstanceId() {
        return instance.instanceId();
    }
}
