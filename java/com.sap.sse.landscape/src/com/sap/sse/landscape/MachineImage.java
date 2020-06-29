package com.sap.sse.landscape;

/**
 * An image (e.g., an AMI for AWS) that can be used to create a {@link Host}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface MachineImage {
    /**
     * The image lives in a region; it can only be used for {@link Host} creation in that region.
     */
    Region getRegion();
}
