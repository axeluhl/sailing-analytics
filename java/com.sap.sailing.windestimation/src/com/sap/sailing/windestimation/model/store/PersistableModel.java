package com.sap.sailing.windestimation.model.store;

import java.io.Serializable;

import com.sap.sailing.windestimation.model.ModelContext;

public interface PersistableModel<InstanceType, T extends ModelContext<InstanceType>>
        extends Serializable {

    PersistenceSupportType getPersistenceSupportType();

    T getModelContext();
}
