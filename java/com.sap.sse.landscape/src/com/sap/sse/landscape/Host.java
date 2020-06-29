package com.sap.sse.landscape;

import java.net.InetAddress;

public interface Host {
    InetAddress getAddress();

    Iterable<Process<? extends Log, ? extends Metrics>> getRunningProcesses();
    
    long getPhysicalRamInBytes();
    
    long getVirtualMemoryInBytes();
    
    int getNumberOfCPUs();
    
    long getNetworkBandwidthInBytesPerSecond();
    
    /**
     * Tells where in the cloud this host runs; the availability zone {@link AvailabilityZone#getRegion() implies} the
     * {@link Region}.
     */
    AvailabilityZone getAvailabilityZone();
    
    Iterable<SecurityGroup> getSecurityGroups();
}
