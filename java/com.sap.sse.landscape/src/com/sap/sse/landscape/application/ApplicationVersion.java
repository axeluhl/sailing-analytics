package com.sap.sse.landscape.application;

import com.sap.sse.common.Named;
import com.sap.sse.common.WithID;

/**
 * An application version can have an optional name derived from the build output name
 * (such as "build-202006300050") and it always has an {@link WithID#getId() ID} that is
 * the {@link String} representing the Git commit ID from which the version was built.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface ApplicationVersion extends Named, WithID {
    /**
     * An application version's ID is the Git commit ID from which it was built
     */
    @Override
    String getId();
}
