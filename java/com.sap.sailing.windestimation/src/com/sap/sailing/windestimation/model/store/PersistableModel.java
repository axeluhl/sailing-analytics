package com.sap.sailing.windestimation.model.store;

import java.io.Serializable;

import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;

public interface PersistableModel<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends Serializable {

    PersistenceSupportType getPersistenceSupportType();

    T getContextSpecificModelMetadata();
}
