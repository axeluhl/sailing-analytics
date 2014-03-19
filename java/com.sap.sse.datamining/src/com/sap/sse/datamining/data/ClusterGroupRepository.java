package com.sap.sse.datamining.data;

import java.util.Collection;

public interface ClusterGroupRepository {

    public void add(ClusterGroup<?> clusterGroup);

    public <T> Collection<ClusterGroup<T>> getClusterGroupsFor(Class<T> type);

}
