package com.sap.sse.landscape.application;

import com.sap.sse.landscape.MachineImage;

/**
 * A special machine image that has what it takes to launch one or more {@link ApplicationProcess}es. When
 * launching, an up-front specification of which {@link ApplicationProcess}es shall be deployed onto this
 * instance can be made.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface ApplicationMachineImage extends MachineImage<ApplicationHost> {
}
