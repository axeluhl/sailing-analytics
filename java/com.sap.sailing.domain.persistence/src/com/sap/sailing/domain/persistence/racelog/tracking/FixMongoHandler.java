package com.sap.sailing.domain.persistence.racelog.tracking;

import com.mongodb.DBObject;
import com.sap.sailing.domain.common.racelog.tracking.TransformationHandler;
import com.sap.sse.common.Timed;

/**
 * For fixes, the fully qualified class name is the type used to identify
 * the handlers, as the fixes are domain objects and are not intended to carry
 * some separate type attribute.
 * 
 * @author Fredrik Teschke
 */
public interface FixMongoHandler<T extends Timed> extends TransformationHandler<T, DBObject> {
}
