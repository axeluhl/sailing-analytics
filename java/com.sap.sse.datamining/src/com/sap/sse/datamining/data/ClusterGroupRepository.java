package com.sap.sse.datamining.data;

import java.io.Serializable;
import java.util.Collection;

public interface ClusterGroupRepository {

    public void add(ClusterGroup<?> clusterGroup);

    public <DataType extends Serializable> Collection<ClusterGroup<DataType>> getClusterGroupsFor(Class<DataType> type);

}
