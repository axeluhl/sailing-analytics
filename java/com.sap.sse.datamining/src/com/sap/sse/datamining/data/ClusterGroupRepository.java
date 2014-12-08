package com.sap.sse.datamining.data;

import java.util.Collection;

public interface ClusterGroupRepository {

    public void add(ClusterGroup<?> clusterGroup);

    public <DataType> Collection<ClusterGroup<DataType>> getClusterGroupsFor(Class<DataType> type);

}
