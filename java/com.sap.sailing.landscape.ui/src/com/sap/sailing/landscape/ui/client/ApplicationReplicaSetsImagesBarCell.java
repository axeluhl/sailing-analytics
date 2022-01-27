package com.sap.sailing.landscape.ui.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sailing.landscape.ui.shared.SailingApplicationReplicaSetDTO;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;

public class ApplicationReplicaSetsImagesBarCell extends ImagesBarCell {
    static final String ACTION_REMOVE = DefaultActions.DELETE.name();
    static final String ACTION_UPGRADE = "UPGRADE";
    static final String ACTION_ARCHIVE = "ARCHIVE";
    static final String ACTION_DEFINE_LANDING_PAGE = "DEFINE_LANDING_PAGE";
    static final String ACTION_CREATE_LOAD_BALANCER_MAPPING = "CREATE_LOAD_BALANGER_MAPPING";
    public static final String ACTION_LAUNCH_ANOTHER_REPLICA_SET_ON_THIS_MASTER = "LAUNCH_ANOTHER_REPLICA_SET_ON_THIS_MASTER";
    public static final String ACTION_ENSURE_ONE_REPLICA_THEN_STOP_REPLICATING_AND_REMOVE_MASTER_FROM_TARGET_GROUPS = "ACTION_ENSURE_ONE_REPLICA_THEN_STOP_REPLICATING_AND_REMOVE_MASTER_FROM_TARGET_GROUPS";
    public static final String ACTION_UPDATE_AMI_FOR_AUTO_SCALING_REPLICAS = "ACTION_UPDATE_AMI_FOR_AUTO_SCALING_REPLICAS";

    private final StringMessages stringMessages;

    public ApplicationReplicaSetsImagesBarCell(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    public ApplicationReplicaSetsImagesBarCell(SafeHtmlRenderer<String> renderer, StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        @SuppressWarnings("unchecked")
        final SailingApplicationReplicaSetDTO<String> applicationReplicaSet = (SailingApplicationReplicaSetDTO<String>) getContext().getKey();
        final List<ImageSpec> result = new ArrayList<>();
        result.add(new ImageSpec(ACTION_ARCHIVE, stringMessages.archive(), IconResources.INSTANCE.archiveIcon()));
        result.add(new ImageSpec(ACTION_REMOVE, stringMessages.remove(), IconResources.INSTANCE.removeIcon()));
        result.add(new ImageSpec(ACTION_DEFINE_LANDING_PAGE, stringMessages.defineLandingPage(),
                IconResources.INSTANCE.editIcon()));
        result.add(new ImageSpec(ACTION_CREATE_LOAD_BALANCER_MAPPING, stringMessages.createLoadBalancerMapping(),
                IconResources.INSTANCE.loadBalancerIcon()));
        result.add(new ImageSpec(ACTION_LAUNCH_ANOTHER_REPLICA_SET_ON_THIS_MASTER,
                stringMessages.launchAnotherReplicaSetOnThisMaster(),
                IconResources.INSTANCE.launchAnotherReplicaSetOnThisMasterIcon()));
        result.add(new ImageSpec(ACTION_UPGRADE, stringMessages.upgrade(), IconResources.INSTANCE.refreshIcon()));
        result.add(new ImageSpec(ACTION_ENSURE_ONE_REPLICA_THEN_STOP_REPLICATING_AND_REMOVE_MASTER_FROM_TARGET_GROUPS,
                stringMessages.ensureAtLeastOneReplicaExistsStopReplicatingAndRemoveMasterFromTargetGroups(),
                IconResources.INSTANCE.unlinkIcon()));
        if (applicationReplicaSet.getAutoScalingGroupAmiId() != null) {
            result.add(new ImageSpec(ACTION_UPDATE_AMI_FOR_AUTO_SCALING_REPLICAS, stringMessages.updateAmiForAutoScalingReplicas(), IconResources.INSTANCE.redGearsIcon()));
        }
        return result;
    }
}
