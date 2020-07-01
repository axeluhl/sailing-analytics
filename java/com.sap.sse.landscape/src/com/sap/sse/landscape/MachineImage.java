package com.sap.sse.landscape;

import com.sap.sse.common.TimePoint;

/**
 * An image (e.g., an AMI for AWS) that can be used to
 * {@link Landscape#launchHost(MachineImage, AvailabilityZone, Iterable) launch} a {@link Host}.
 * 
 * @param <HostT>
 *            the type of host that one gets when {@link Landscape#launchHost(MachineImage, AvailabilityZone, Iterable)
 *            launching} a host off this image
 * @author Axel Uhl (D043530)
 *
 */
public interface MachineImage<HostT extends Host> {
    /**
     * The image lives in a region; it can only be used for {@link Host} creation in that region.
     */
    Region getRegion();
    
    TimePoint getCreatedAt();
    
    /**
     * Upgrades this machine image by running a package manager update which is expected to also
     * upgrade the operating system kernel and apply all security patches.
     * 
     * @return the new machine image that has the updated packages
     */
    MachineImage<HostT> updateAllPackages();
    
    /**
     * Deletes this machine image from the infrastructure. When this method returns, this object cannot
     * be used anymore to create a new instance.
     */
    void delete();
}
