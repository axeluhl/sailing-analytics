package com.sap.sse.landscape;

import java.net.InetAddress;

import com.sap.sse.common.Duration;

public interface Host {
    /**
     * Obtains the public IP address of this host. Note that during the boot phase a host may not yet have such
     * a public IP address assigned. In this case, {@code null} may be returned. To avoid this, you can alternatively
     * call {@link #getPublicAddress(long)} to wait for a public IP address to become available.
     */
    InetAddress getPublicAddress();
    
    /**
     * Obtains the public IP address of this host, waiting for one to become available for the duration of
     * {@code timeout}, or forever in case {@code timeout} is {@code null}
     * 
     * @param timeoutNullMeaningForever
     *            if {@code null}, waits forever
     */
    InetAddress getPublicAddress(Duration timeoutNullMeaningForever);

    Iterable<? extends Process<? extends Log, ? extends Metrics>> getRunningProcesses();
    
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
