package com.sap.sse.datamining.impl.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.data.ClusterGroupRepository;

public class SimpleClusterGroupRepository implements ClusterGroupRepository {
    
    private static final Logger LOGGER = Logger.getLogger(SimpleClusterGroupRepository.class.getSimpleName());
    
    private HashMap<Class<?>, Collection<ClusterGroup<?>>> clusterGroupsMappedByType;

    public SimpleClusterGroupRepository() {
        clusterGroupsMappedByType = new HashMap<>();
    }

    @Override
    public void add(ClusterGroup<?> clusterGroup) {
        Class<?> clusterElementsType = clusterGroup.getClusterElementsType();
        if (!clusterGroupsMappedByType.containsKey(clusterElementsType)) {
            clusterGroupsMappedByType.put(clusterElementsType, new HashSet<ClusterGroup<?>>());
        }
        clusterGroupsMappedByType.get(clusterElementsType).add(clusterGroup);
    }

    @SuppressWarnings("unchecked") // Necessary because you can't use instanceof with generics
    @Override
    public <T extends Serializable> Collection<ClusterGroup<T>> getClusterGroupsFor(Class<T> type) {
        Collection<ClusterGroup<T>> specificClusterGroups = new HashSet<>();
        if (clusterGroupsMappedByType.containsKey(type)) {
            for (ClusterGroup<?> clusterGroup : clusterGroupsMappedByType.get(type)) {
                try {
                    specificClusterGroups.add((ClusterGroup<T>) clusterGroup);
                } catch (ClassCastException e) {
                    LOGGER.log(Level.FINER, "Error casting the stored cluster groups:", e);
                }
            }
        }
        return specificClusterGroups;
    }

}
