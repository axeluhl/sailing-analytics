package com.sap.sse.datamining.impl.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.data.ClusterGroupRepository;

public class CompoundClusterGroupRepository implements ClusterGroupRepository {

    private Collection<ClusterGroupRepository> repositories;
    private SimpleClusterGroupRepository internalRepository;

    public CompoundClusterGroupRepository(Collection<ClusterGroupRepository> repositories) {
        this.repositories = new HashSet<>(repositories);
        
        this.internalRepository = new SimpleClusterGroupRepository();
        this.repositories.add(internalRepository);
    }

    @Override
    public void add(ClusterGroup<?> clusterGroup) {
        internalRepository.add(clusterGroup);
    }

    @Override
    public <T extends Serializable> Collection<ClusterGroup<T>> getClusterGroupsFor(Class<T> type) {
        Collection<ClusterGroup<T>> allClusterGroups = new HashSet<>();
        for (ClusterGroupRepository clusterGroupRepository : repositories) {
            allClusterGroups.addAll(clusterGroupRepository.getClusterGroupsFor(type));
        }
        return allClusterGroups;
    }

}
